package cats
package mtl
package instances

import cats.data.WriterT

trait TellingInstances extends TellingInstancesLowPriority {
  implicit def tellInd[M[_], Inner[_], L](implicit
                                          lift: monad.Layer[M, Inner],
                                          under: monad.Telling[Inner, L]
                                         ): monad.Telling[M, L] = {
    new monad.Telling[M, L] {
      val monad: Monad[M] = lift.outerMonad
      val monoid: Monoid[L] = under.monoid

      def tell(l: L): M[Unit] = lift.layer(under.tell(l))

      def writer[A](a: A, l: L): M[A] = lift.layer(under.writer(a, l))
    }
  }
}

trait TellingInstancesLowPriority {

  implicit def tellWriter[M[_], L](implicit L: Monoid[L], M: Monad[M]): monad.Telling[CurryT[WriterTCL[L]#l, M]#l, L] = {
    new monad.Telling[CurryT[WriterTCL[L]#l, M]#l, L] {
      val monad = WriterT.catsDataMonadWriterForWriterT(M, L)
      val monoid: Monoid[L] = L

      def tell(l: L): WriterT[M, L, Unit] = {
        WriterT.tell(l)
      }

      def writer[A](a: A, l: L): WriterT[M, L, A] = {
        WriterT.put(a)(l)
      }
    }
  }

}

object telling extends TellingInstances
