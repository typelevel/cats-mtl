package cats
package mtl
package instances

import cats.data.WriterT

trait WriterTInstances extends WriterTInstancesLowPriority {
  implicit final def writerMonadLayer[M[_], L](implicit L: Monoid[L], M: Monad[M]): MonadLayer[CurryT[WriterTCL[L]#l, M]#l, M] =
    writerMonadTransControl[M, L]
}

private[instances] trait WriterTInstancesLowPriority {
  implicit final def writerMonadTransControl[M[_], L]
  (implicit L: Monoid[L], M: Monad[M]): MonadTransControl.Aux[CurryT[WriterTCL[L]#l, M]#l, TupleC[L]#l, M, WriterTCL[L]#l] = {
    new MonadTransControl[CurryT[WriterTCL[L]#l, M]#l, M] {
      type State[A] = (L, A)

      type Inner[A] = M[A]

      type Outer[F[_], A] = WriterT[F, L, A]

      def restore[A](state: (L, A)): WriterT[M, L, A] = WriterT(M.pure(state))

      def zero[A](state: (L, A)): Boolean = false

      val monad: Monad[CurryT[WriterTCL[L]#l, M]#l] =
        WriterT.catsDataMonadWriterForWriterT
      val innerMonad: Monad[M] = M

      def transControl[A](cps: MonadTransContinuation[State, Outer, A]): WriterT[M, L, A] = {
        WriterT.lift[M, L, A](
          cps(new (WriterTC[M, L]#l ~> (M of TupleC[L]#l)#l) {
            def apply[X](fa: WriterT[M, L, X]): M[(L, X)] = fa.run
          })(this)
        )
      }

      def layerControl[A](cps: (WriterTC[M, L]#l ~> (M of TupleC[L]#l)#l) => M[A]): WriterT[M, L, A] = {
        WriterT.lift[M, L, A](
          cps(new (WriterTC[M, L]#l ~> (M of TupleC[L]#l)#l) {
            def apply[X](fa: WriterT[M, L, X]): M[(L, X)] = fa.run
          })
        )
      }

      def layerMap[A](ma: WriterT[M, L, A])(trans: M ~> M): WriterT[M, L, A] = WriterT(trans(ma.run))

      def layer[A](inner: M[A]): WriterT[M, L, A] = WriterT.lift(inner)

      def imapK[A](ma: WriterT[M, L, A])(forward: M ~> M, backward: M ~> M): WriterT[M, L, A] = layerMap(ma)(forward)

      def showLayers[F[_], A](ma: F[WriterT[M, L, A]]): F[WriterT[M, L, A]] = ma

      def hideLayers[F[_], A](foia: F[WriterT[M, L, A]]): F[WriterT[M, L, A]] = foia

      def transInvMap[N[_], NInner[_], A]
      (ma: WriterT[M, L, A])(forward: M ~> NInner, backward: NInner ~> M)
      (implicit other: MonadTrans.AuxIO[N, NInner, WriterTCL[L]#l]): N[A] = {
        transMap(ma)(forward)
      }

      def transMap[A, N[_], NInner[_]]
      (ma: WriterT[M, L, A])(trans: M ~> NInner)
      (implicit mt: MonadTrans.AuxIO[N, NInner, WriterTCL[L]#l]): N[A] = {
        mt.hideLayers[Id, A](WriterT(trans(ma.run)))
      }

    }
  }
}

object writert extends WriterTInstances {

}
