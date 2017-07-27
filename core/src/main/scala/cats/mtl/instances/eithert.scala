package cats
package mtl
package instances

import cats.data.EitherT

trait EitherTInstances extends EitherTInstances0 {
  implicit final def eitherMonadLayerControl[M[_], E]
  (implicit M: Monad[M]): MonadLayerControl.Aux[EitherTC[M, E]#l, M, EitherC[E]#l] = {
    new MonadLayerControl[EitherTC[M, E]#l, M] {
      type State[A] = E Either A

      val outerInstance: Monad[EitherTC[M, E]#l] =
        EitherT.catsDataMonadErrorForEitherT

      val innerInstance: Monad[M] = M

      def layerMapK[A](ma: EitherT[M, E, A])(trans: M ~> M): EitherT[M, E, A] = EitherT(trans(ma.value))

      def layer[A](inner: M[A]): EitherT[M, E, A] = EitherT.right(inner)

      def restore[A](state: Either[E, A]): EitherT[M, E, A] = EitherT.fromEither[M](state)

      def layerControl[A](cps: (EitherTC[M, E]#l ~> (M of EitherC[E]#l)#l) => M[A]): EitherT[M, E, A] = {
        EitherT.right(cps(new (EitherTC[M, E]#l ~> (M of EitherC[E]#l)#l) {
          def apply[X](fa: EitherT[M, E, X]): M[Either[E, X]] = fa.value
        }))
      }

      def zero[A](state: Either[E, A]): Boolean = state.isLeft
    }
  }
}

trait EitherTInstances0 {
  implicit def eitherFunctorLayerFunctor[M[_], E]
  (implicit M: Functor[M]): FunctorLayerFunctor[EitherTC[M, E]#l, M] = {
    new FunctorLayerFunctor[EitherTC[M, E]#l, M] {
      def layerMapK[A](ma: EitherT[M, E, A])(trans: M ~> M): EitherT[M, E, A] = EitherT(trans(ma.value))

      val outerInstance: Functor[EitherTC[M, E]#l] = EitherT.catsDataFunctorForEitherT(M)
      val innerInstance: Functor[M] = M

      def layer[A](inner: M[A]): EitherT[M, E, A] = EitherT.right(inner)
    }
  }
}

object eithert extends EitherTInstances
