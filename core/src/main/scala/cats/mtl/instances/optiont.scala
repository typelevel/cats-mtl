package cats
package mtl
package instances

import cats.data.OptionT

trait OptionTInstances extends OptionTInstancesLowPriority {
}

private[instances] trait OptionTInstancesLowPriority {
  implicit final def optionMonadLayerControl[M[_]]
  (implicit M: Monad[M]): monad.LayerControl.Aux[OptionTC[M]#l, M, Option] = {
    new monad.LayerControl[OptionTC[M]#l, M] {
      type State[A] = Option[A]

      val outerInstance: Monad[OptionTC[M]#l] =
        OptionT.catsDataMonadForOptionT

      val innerInstance: Monad[M] = M

      def layerMapK[A](ma: OptionT[M, A])(trans: M ~> M): OptionT[M, A] = OptionT(trans(ma.value))

      def layer[A](inner: M[A]): OptionT[M, A] = OptionT.liftF(inner)

      def restore[A](state: Option[A]): OptionT[M, A] = OptionT.fromOption[M](state)

      def layerControl[A](cps: (OptionTC[M]#l ~> (M of Option)#l) => M[A]): OptionT[M, A] =
        OptionT.liftF(cps(new (OptionTC[M]#l ~> (M of Option)#l) {
          def apply[X](fa: OptionT[M, X]): M[Option[X]] = fa.value
        }))

      def zero[A](state: Option[A]): Boolean = state.isEmpty
    }
  }
}

object optiont extends OptionTInstances
