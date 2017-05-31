package cats
package mtl
package instances

import cats.data.EitherT
import cats.mtl.monad.{Layer, Raising}

trait RaisingInstances extends RaisingLowPriorityInstances {
  implicit def raiseNIndT[T[_[_], _], M[_], E]
  (implicit lift: monad.Trans.Aux[CurryT[T, M]#l, M, T],
   under: Raising[M, E]): Raising[CurryT[T, M]#l, E] =
    raiseNInd[CurryT[T, M]#l, M, E](lift, under)
}

trait RaisingLowPriorityInstances extends RaisingLowPriorityInstances1 {
  implicit def raiseNInd[M[_], Inner[_], E](implicit
                                            lift: Layer[M, Inner],
                                            under: Raising[Inner, E]
                                           ): Raising[M, E] =
    new Raising[M, E] {
      val monad = lift.outerMonad
      def raise[A](e: E): M[A] =
        lift.layer(under.raise(e))
    }
}

trait RaisingLowPriorityInstances1 {
  implicit def raiseNEither[M[_], E](implicit M: Monad[M]): Raising[EitherTC[M, E]#l, E] =
    new Raising[EitherTC[M, E]#l, E] {
      val monad = EitherT.catsDataMonadErrorForEitherT
      def raise[A](e: E): EitherT[M, E, A] =
        EitherT(M.pure(Left(e)))
    }

}

object raising extends RaisingInstances
