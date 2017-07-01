package cats
package mtl
package instances

import cats.data.EitherT
import cats.syntax.cartesian._

trait EitherTInstances extends EitherTInstances0 {
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

trait EitherTInstances0 extends EitherTInstances1 {
  implicit def eitherApplicativeLayerFunctor[M[_], E]
  (implicit M: Applicative[M]): ApplicativeLayerFunctor[EitherTC[M, E]#l, M] = {
    new ApplicativeLayerFunctor[EitherTC[M, E]#l, M] {
      def layerMapK[A](ma: EitherT[M, E, A])(trans: M ~> M): EitherT[M, E, A] = EitherT(trans(ma.value))

      val outerInstance: Applicative[EitherTC[M, E]#l] = new Applicative[EitherTC[M, E]#l] {
        def pure[A](x: A): EitherT[M, E, A] = EitherT.pure(x)

        def ap[A, B](ff: EitherT[M, E, (A) => B])(fa: EitherT[M, E, A]): EitherT[M, E, B] = {
          EitherT((ff.value |@| fa.value).map((fe, ae) => fe.right.flatMap(f => ae.right.map(f))))
        }
      }
      val innerInstance: Applicative[M] = M

      def layer[A](inner: M[A]): EitherT[M, E, A] = EitherT.right(inner)
    }
  }
}

trait EitherTInstances1 {
  implicit def eitherFunctorLayerFunctor[M[_], E]
  (implicit M: Functor[M]): FunctorLayer[EitherTC[M, E]#l, M] = {
    new FunctorLayerFunctor[EitherTC[M, E]#l, M] {
      def layerMapK[A](ma: EitherT[M, E, A])(trans: M ~> M): EitherT[M, E, A] = EitherT(trans(ma.value))

      val outerInstance: Functor[EitherTC[M, E]#l] = EitherT.catsDataFunctorForEitherT(M)
      val innerInstance: Functor[M] = M

      def layer[A](inner: M[A]): EitherT[M, E, A] = EitherT.right(inner)
    }
  }
}

object eithert extends EitherTInstances
