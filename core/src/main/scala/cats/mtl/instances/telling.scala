package cats
package mtl
package instances

import cats.data.WriterT
import cats.mtl.monad.{Layer, Telling}

trait TellingInstances extends TellingInstancesLowPriority {
  implicit def tellInd[M[_], Inner[_], L](implicit
                                          lift: Layer[M, Inner],
                                          under: Telling[Inner, L]
                                         ): Telling[M, L] =
    new Telling[M, L] {
      val monad: Monad[M] = lift.monad

      def tell(l: L): M[Unit] = lift.layer(under.tell(l))

      def writer[A](a: A, l: L): M[A] = lift.layer(under.writer(a, l))
    }
}

trait TellingInstancesLowPriority {

  implicit def tellWriter[M[_], L](implicit L: Monoid[L], M: Monad[M]): Telling[CurryT[WriterTCL[L]#l, M]#l, L] =
    new Telling[CurryT[WriterTCL[L]#l, M]#l, L] {
      val monad: Monad[M] = M

      def tell(l: L): WriterT[M, L, Unit] =
        WriterT.tell(l)

      def writer[A](a: A, l: L): WriterT[M, L, A] =
        WriterT.put(a)(l)
    }

}

object telling extends TellingInstances
