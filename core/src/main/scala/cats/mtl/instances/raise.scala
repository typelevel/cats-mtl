package cats
package mtl
package instances

import cats.data.EitherT
import cats.mtl.FunctorRaise
import cats.mtl.lifting.FunctorLayer

trait RaiseInstances extends RaiseLowPriorityInstances1 {
  implicit final def raiseInd[M[_], Inner[_], E](implicit
                                                   lift: FunctorLayer[M, Inner],
                                                   under: FunctorRaise[Inner, E]
                                                 ): FunctorRaise[M, E] = {
    new FunctorRaise[M, E] {
      val functor = lift.outerInstance

      def raise[A](e: E): M[A] = lift.layer(under.raise(e))
    }
  }
}

private[instances] trait RaiseLowPriorityInstances1 {
  implicit final def raiseEitherT[M[_], E](implicit M: Applicative[M]): FunctorRaise[EitherTC[M, E]#l, E] = {
    new FunctorRaise[EitherTC[M, E]#l, E] {
      val functor: Functor[EitherTC[M, E]#l] = EitherT.catsDataFunctorForEitherT(M)

      def raise[A](e: E): EitherT[M, E, A] = EitherT(M.pure(Left(e)))
    }
  }

  implicit final def raiseEither[E]: FunctorRaise[EitherC[E]#l, E] = {
    new FunctorRaise[EitherC[E]#l, E] {
      val functor: Functor[EitherC[E]#l] = cats.instances.either.catsStdInstancesForEither

      def raise[A](e: E): Either[E, A] = Left(e)
    }
  }

  // TODO: an instance derived from another FunctorRaise instance and an Inject instance ("subtyping")
}

object raise extends RaiseInstances
