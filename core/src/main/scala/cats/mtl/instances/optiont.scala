package cats
package mtl
package instances

import cats.data.OptionT

trait OptionTInstances extends OptionTInstances1 {
  implicit def optionFunctorLayerFunctor[M[_]]
  (implicit M: Functor[M]): FunctorLayerFunctor[OptionTC[M]#l, M] = {
    new FunctorLayerFunctor[OptionTC[M]#l, M] {
      def layerMapK[A](ma: OptionT[M, A])(trans: M ~> M): OptionT[M, A] = OptionT(trans(ma.value))

      val outerInstance: Functor[OptionTC[M]#l] = OptionT.catsDataFunctorForOptionT(M)
      val innerInstance: Functor[M] = M

      def layer[A](inner: M[A]): OptionT[M, A] = OptionT.liftF(inner)
    }
  }

}

trait OptionTInstances1 extends OptionTInstances2 {
  implicit def optionApplicativeLayerFunctor[M[_]]
  (implicit M: Applicative[M]): ApplicativeLayerFunctor[OptionTC[M]#l, M] = {
    new ApplicativeLayerFunctor[OptionTC[M]#l, M] {
      def layerMapK[A](ma: OptionT[M, A])(trans: M ~> M): OptionT[M, A] = OptionT(trans(ma.value))

      val outerInstance: Applicative[OptionTC[M]#l] = new Applicative[OptionTC[M]#l] {
        def pure[A](x: A): OptionT[M, A] = OptionT.pure(x)

        def ap[A, B](ff: OptionT[M, (A) => B])(fa: OptionT[M, A]): OptionT[M, B] = OptionT[M, B] {
          M.map2(ff.value, fa.value){
            case (Some(f), Some(a)) => Some(f(a))
            case _ => None
          }
        }
      }
      val innerInstance: Applicative[M] = M

      def layer[A](inner: M[A]): OptionT[M, A] = OptionT.liftF(inner)
    }
  }
}

private[instances] trait OptionTInstances2 {
  implicit final def optionMonadLayerControl[M[_]]
  (implicit M: Monad[M]): MonadLayerControl.Aux[OptionTC[M]#l, M, Option] = {
    new MonadLayerControl[OptionTC[M]#l, M] {
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
