package cats
package mtl
package instances

import cats.data.{Reader, ReaderT}
import cats.mtl.MonadTrans.{AuxI, AuxIO}

object reader {

  def readerMonadTransControl[M[_], E](implicit M: Monad[M]): MonadTransControl[CurryT[ReaderTCE[E]#l, M]#l] =
    new MonadTransControl[CurryT[ReaderTCE[E]#l, M]#l] {
      override type State[A] = Reader[E, A]

      override type Inner[A] = M[A]

      override type Outer[F[_], A] = ReaderT[F, E, A]

      override def transControl[N[_], NInner[_], A](cps: (N ~> (NInner of ReaderC[E]#l)#l) => NInner[A])
                                                   (implicit mt: AuxIO[N, NInner, ReaderTCE[E]#l]): ReaderT[M, E, A] = {
        ReaderT.apply[M, E, N[A]] { (r: E) =>
          val x: NInner[A] =
              cps(new (N ~> (NInner of ReaderC[E]#l)#l) {
                def apply[A](fa: N[A]): NInner[A] =
                  mt.showLayers[Id, A](fa).run(r)
              })
          x
        }
      }

      def restore[A](state: Reader[E, A]): ReaderT[M, E, A] =
        state.mapF(M.pure)

      def layerControl[A](cps: (ReaderTC[M, E]#l ~> (M of ReaderC[E]#l)) => M[A]): ReaderT[M, E, A] = ???

      def zero[A](state: Reader[E, A]): Boolean = false

      def layerMap[A](ma: ReaderT[M, E, A])(trans: M ~> M): ReaderT[M, E, A] =
        ma.transform(trans)

      val monad: Monad[M] = M
      val innerMonad: Monad[CurryT[ReaderTCE[E]#l, M]#l] = ReaderT.catsDataMonadReaderForKleisli

      def layer[A](inner: M[A]): ReaderT[M, E, A] = ???

      def imapK[A](ma: ReaderT[M, E, A])(forward: ~>[M, M], backward: ~>[M, M]): ReaderT[M, E, A] = ???
    }


}
