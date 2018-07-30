package cats
package mtl
package instances

import cats.mtl.lifting.ApplicativeLayer

trait TellInstances {
  implicit final def tellInd[M[_], Inner[_], L](implicit
                                                   lift: ApplicativeLayer[M, Inner],
                                                   under: FunctorTell[Inner, L]
                                               ): FunctorTell[M, L] = {
    new FunctorTell[M, L] {
      val functor: Functor[M] = lift.outerInstance

      def tell(l: L): M[Unit] = lift.layer(under.tell(l))

      def writer[A](a: A, l: L): M[A] = lift.layer(under.writer(a, l))

      def tuple[A](ta: (L, A)): M[A] = lift.layer(under.tuple(ta))
    }
  }
}

object tell extends TellInstances
