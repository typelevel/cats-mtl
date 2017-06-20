package cats
package mtl
package instances

import cats.data.EitherT
import cats.mtl.functor.Raising

trait RaisingInstances extends RaisingLowPriorityInstances {
  implicit final def raiseNIndT[T[_[_], _], M[_], E]
  (implicit lift: monad.Trans.Aux[CurryT[T, M]#l, M, T],
   under: Raising[M, E]): Raising[CurryT[T, M]#l, E] = {
    raiseNInd[CurryT[T, M]#l, M, E](lift, under)
  }
}

private[instances] trait RaisingLowPriorityInstances extends RaisingLowPriorityInstances1 {
  implicit final def raiseNInd[M[_], Inner[_], E](implicit
                                                  lift: monad.Layer[M, Inner],
                                                  under: Raising[Inner, E]
                                                 ): Raising[M, E] = {
    new Raising[M, E] {
      val functor = lift.outerInstance

      def raise[A](e: E): M[A] = {
        lift.layer(under.raise(e))
      }
    }
  }
}

private[instances] trait RaisingLowPriorityInstances1 {
  implicit final def raiseNEither[M[_], E](implicit M: Monad[M]): Raising[EitherTC[M, E]#l, E] = {
    new Raising[EitherTC[M, E]#l, E] {
      val functor = EitherT.catsDataFunctorForEitherT

      def raise[A](e: E): EitherT[M, E, A] = {
        EitherT(M.pure(Left(e)))
      }
    }
  }

}

object raising extends RaisingInstances
