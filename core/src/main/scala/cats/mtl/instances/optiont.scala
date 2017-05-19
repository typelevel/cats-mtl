package cats
package mtl
package instances

import cats.data.OptionT

trait OptionTInstances extends OptionTInstancesLowPriority {
  implicit final def optionMonadLayer[M[_]](implicit M: Monad[M]): MonadLayer.Aux[OptionTC[M]#l, M] =
    optionMonadTransControl[M]
}

trait OptionTInstancesLowPriority {

  implicit final def optionMonadTransControl[M[_]]
  (implicit M: Monad[M]): MonadTransControl.Aux[OptionTC[M]#l, Option, M, OptionT] = {
    new MonadTransControl[OptionTC[M]#l] {
      type State[A] = Option[A]
      type Inner[A] = M[A]
      type Outer[F[_], A] = OptionT[F, A]

      def restore[A](state: Option[A]): OptionT[M, A] = OptionT.fromOption(state)

      def zero[A](state: Option[A]): Boolean = state.isEmpty

      val monad: Monad[OptionTC[M]#l] =
        OptionT.catsDataMonadForOptionT
      val innerMonad: Monad[M] = M

      def transControl[A](cps: MonadTransContinuation[State, Outer, A]): OptionT[M, A] = {
        OptionT.liftF(
          cps(new (OptionTC[M]#l ~> (M of Option)#l) {
            def apply[X](fa: OptionT[M, X]): M[Option[X]] = fa.value
          })(this)
        )
      }

      def layerControl[A](cps: (OptionTC[M]#l ~> (M of Option)#l) => M[A]): OptionT[M, A] = {
        OptionT.liftF(
          cps(new (OptionTC[M]#l ~> (M of Option)#l) {
            def apply[X](fa: OptionT[M, X]): M[Option[X]] = fa.value
          })
        )
      }

      def layerMap[A](ma: OptionT[M, A])(trans: M ~> M): OptionT[M, A] = OptionT(trans(ma.value))

      def layer[A](inner: M[A]): OptionT[M, A] = OptionT.liftF(inner)

      def imapK[A](ma: OptionT[M, A])(forward: M ~> M, backward: M ~> M): OptionT[M, A] = layerMap(ma)(forward)

      def showLayers[F[_], A](ma: F[OptionT[M, A]]): F[OptionT[M, A]] = ma

      def hideLayers[F[_], A](foia: F[OptionT[M, A]]): F[OptionT[M, A]] = foia

      def transInvMap[N[_], NInner[_], A]
      (ma: OptionT[M, A])(forward: M ~> NInner, backward: NInner ~> M)
      (implicit other: MonadTrans.AuxIO[N, NInner, OptionT]): N[A] = {
        transMap(ma)(forward)
      }

      def transMap[A, N[_], NInner[_]]
      (ma: OptionT[M, A])(trans: M ~> NInner)
      (implicit mt: MonadTrans.AuxIO[N, NInner, OptionT]): N[A] = {
        mt.hideLayers[Id, A](OptionT(trans(ma.value)))
      }

    }
  }
}

object optiont {

}
