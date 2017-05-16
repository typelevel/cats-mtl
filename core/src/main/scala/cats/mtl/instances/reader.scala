package cats
package mtl
package instances

import cats.data.ReaderT
import cats.syntax.all._
import cats.mtl.MonadTrans.AuxIO

object reader {

  def readerMonadTransControl[M[_], E](implicit M: Monad[M]): MonadTransControl[CurryT[ReaderTCE[E]#l, M]#l] = {
    new MonadTransControl[CurryT[ReaderTCE[E]#l, M]#l] {
      type State[A] = A

      type Inner[A] = M[A]

      type Outer[F[_], A] = ReaderTCE[E]#l[F, A]

      def transControl[A](cps: MonadTransContinuation[Id, Outer, A]): ReaderT[M, E, A] = {
        ReaderT[M, E, A]((r: E) =>
          cps(new (ReaderTC[M, E]#l ~> M) {
            def apply[X](rea: ReaderT[M, E, X]): M[X] = rea.run(r)
          })(this)
        )
      }

      def restore[A](state: A): ReaderT[M, E, A] = ReaderT.pure(state)

      def layerControl[A](cps: (ReaderTC[M, E]#l ~> M) => M[A]): ReaderT[M, E, A] = {
        ReaderT[M, E, A]((r: E) =>
          cps(new (ReaderTC[M, E]#l ~> M) {
            def apply[X](rea: ReaderT[M, E, X]): M[X] = rea.run(r)
          })
        )
      }

      def zero[A](state: A): Boolean = false

      def layerMap[A](ma: ReaderT[M, E, A])(trans: M ~> M): ReaderT[M, E, A] = ma.transform(trans)

      val monad: Monad[CurryT[ReaderTCE[E]#l, M]#l] = ReaderT.catsDataMonadReaderForKleisli
      val innerMonad: Monad[(M of CurryT[ReaderTCE[E]#l, M]#l)#l] = new Monad[(M of CurryT[ReaderTCE[E]#l, M]#l)#l] {
        def pure[A](x: A): M[ReaderT[M, E, A]] = M.pure(ReaderT.pure(x))

        def flatMap[A, B](fa: M[ReaderT[M, E, A]])(f: (A) => M[ReaderT[M, E, B]]): M[ReaderT[M, E, B]] = {
          for {
            r <- fa
            a = ReaderT((e: E) => M.flatMap(r(e))(f))
          } yield monad.flatten(a)
        }

        def tailRecM[A, B](a: A)(f: (A) => M[ReaderT[M, E, Either[A, B]]]): M[ReaderT[M, E, B]] = {
          M.pure(monad.tailRecM(a) { e =>
            ReaderT((env: E) =>
              f(e).flatMap(_.run(env))
            )
          })
        }
      }

      def layer[A](inner: M[A]): ReaderT[M, E, A] = {
        ReaderT.lift(inner)
      }

      def imapK[A](ma: ReaderT[M, E, A])(forward: M ~> M, backward: M ~> M): ReaderT[M, E, A] = {
        layerMap(ma)(forward)
      }

      def showLayers[F[_], A](ma: F[ReaderT[M, E, A]]): F[ReaderT[M, E, A]] = ma

      def hideLayers[F[_], A](foia: F[ReaderT[M, E, A]]): F[ReaderT[M, E, A]] = foia

      def transInvMap[N[_], NInner[_], A](ma: ReaderT[M, E, A])(forward: M ~> NInner, backward: NInner ~> M)
                                         (implicit other: MonadTrans.AuxIO[N, NInner, ReaderTCE[E]#l]): N[A] = {
        transMap(ma)(forward)
      }

      def transMap[A, N[_], NInner[_]](ma: ReaderT[M, E, A])(trans: M ~> NInner)
                                      (implicit mt: AuxIO[N, NInner, ReaderTCE[E]#l]): N[A] = {
        mt.hideLayers[Id, A](ma.transform(trans))
      }
    }
  }

}
