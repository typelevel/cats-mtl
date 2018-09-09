package cats
package mtl
package instances


import cats.mtl.lifting.FunctorLayer

trait RaiseInstances {
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

object raise extends RaiseInstances
