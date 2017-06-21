package cats
package mtl
package instances

import cats.data.EitherT
import cats.mtl.functor.Raising

trait RaisingInstances extends RaisingLowPriorityInstances1 {
  implicit final def raisingInd[M[_], Inner[_], E](implicit
                                                  lift: functor.Layer[M, Inner],
                                                  under: Raising[Inner, E]
                                                 ): Raising[M, E] = {
    new Raising[M, E] {
      val functor = lift.outerInstance

      def raise[A](e: E): M[A] = lift.layer(under.raise(e))
    }
  }
}

private[instances] trait RaisingLowPriorityInstances1 {
  implicit final def raisingEitherT[M[_], E](implicit M: Applicative[M]): Raising[EitherTC[M, E]#l, E] = {
    new Raising[EitherTC[M, E]#l, E] {
      val functor = EitherT.catsDataFunctorForEitherT

      def raise[A](e: E): EitherT[M, E, A] = EitherT(M.pure(Left(e)))
    }
  }

}

object raising extends RaisingInstances
