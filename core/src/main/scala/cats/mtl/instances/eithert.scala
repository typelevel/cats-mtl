package cats
package mtl
package instances

import cats.data.EitherT

trait EitherTInstances extends EitherTInstancesLowPriority {
  implicit final def eitherMonadLayer[M[_], E]
  (implicit M: Monad[M]): MonadLayer[EitherTC[M, E]#l, M] = {
    eitherMonadLayerControl[M, E]
  }
}

private[instances] trait EitherTInstancesLowPriority {
  implicit final def eitherMonadLayerControl[M[_], E]
  (implicit M: Monad[M]): MonadLayerControl.Aux[EitherTC[M, E]#l, M, EitherC[E]#l] = {
    new MonadLayerControl[EitherTC[M, E]#l, M] {
      type State[A] = E Either A

      val outerInstance: Monad[CurryT[EitherTCE[E]#l, M]#l] =
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

object eithert extends EitherTInstances
