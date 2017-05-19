package cats
package mtl
package instances

import cats.data.ReaderT

trait ReaderTInstances extends ReaderTInstancesLowPriority {
  implicit final def readerMonadLayer[M[_], E](implicit M: Monad[M]): MonadLayer.Aux[CurryT[ReaderTCE[E]#l, M]#l, M] =
    readerMonadTransControl[M, E]
}

trait ReaderTInstancesLowPriority {

  implicit final def readerMonadTransControl[M[_], E](implicit M: Monad[M]): MonadTransControl.Aux[CurryT[ReaderTCE[E]#l, M]#l, Id, M, ReaderTCE[E]#l] = {
    new MonadTransControl[CurryT[ReaderTCE[E]#l, M]#l] {
      type State[A] = A

      type Inner[A] = M[A]

      type Outer[F[_], A] = ReaderTCE[E]#l[F, A]

      def restore[A](state: A): ReaderT[M, E, A] = ReaderT.pure(state)

      def zero[A](state: A): Boolean = false

      val monad: Monad[CurryT[ReaderTCE[E]#l, M]#l] = ReaderT.catsDataMonadReaderForKleisli
      val innerMonad: Monad[M] = M

      def layerControl[A](cps: (ReaderTC[M, E]#l ~> M) => M[A]): ReaderT[M, E, A] = {
        ReaderT[M, E, A]((r: E) =>
          cps(new (ReaderTC[M, E]#l ~> M) {
            def apply[X](rea: ReaderT[M, E, X]): M[X] = rea.run(r)
          })
        )
      }

      def transControl[A](cps: MonadTransContinuation[Id, Outer, A]): ReaderT[M, E, A] = {
        ReaderT[M, E, A]((r: E) =>
          cps(new (ReaderTC[M, E]#l ~> M) {
            def apply[X](rea: ReaderT[M, E, X]): M[X] = rea.run(r)
          })(this)
        )
      }

      def layerMap[A](ma: ReaderT[M, E, A])(trans: M ~> M): ReaderT[M, E, A] = ma.transform(trans)

      def layer[A](inner: M[A]): ReaderT[M, E, A] = ReaderT.lift(inner)

      def imapK[A](ma: ReaderT[M, E, A])(forward: M ~> M, backward: M ~> M): ReaderT[M, E, A] = layerMap(ma)(forward)

      def showLayers[F[_], A](ma: F[ReaderT[M, E, A]]): F[ReaderT[M, E, A]] = ma

      def hideLayers[F[_], A](foia: F[ReaderT[M, E, A]]): F[ReaderT[M, E, A]] = foia

      def transInvMap[N[_], NInner[_], A]
      (ma: ReaderT[M, E, A])(forward: M ~> NInner, backward: NInner ~> M)
      (implicit other: MonadTrans.AuxIO[N, NInner, ReaderTCE[E]#l]): N[A] = {
        transMap(ma)(forward)
      }

      def transMap[A, N[_], NInner[_]]
      (ma: ReaderT[M, E, A])(trans: M ~> NInner)
      (implicit mt: MonadTrans.AuxIO[N, NInner, ReaderTCE[E]#l]): N[A] = {
        mt.hideLayers[Id, A](ma.transform(trans))
      }
    }
  }

}

object readert extends ReaderTInstances
