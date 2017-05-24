package cats
package mtl
package instances

import cats.data.WriterT

trait TellInstances extends TellInstancesLowPriority {
  implicit def tellInd[M[_], Inner[_], L](implicit
                                          lift: MonadLayer[M, Inner],
                                          under: Telling[Inner, L]
                                         ): Telling[M, L] =
    new Telling[M, L] {
      def tell(l: L): M[Unit] = lift.layer(under.tell(l))

      def writer[A](a: A, l: L): M[A] = lift.layer(under.writer(a, l))
    }
}

trait TellInstancesLowPriority {

  implicit def tellWriter[M[_], L](implicit L: Monoid[L], M: Monad[M]): Telling[CurryT[WriterTCL[L]#l, M]#l, L] =
    new Telling[CurryT[WriterTCL[L]#l, M]#l, L] {
      def tell(l: L): WriterT[M, L, Unit] =
        WriterT.tell(l)

      def writer[A](a: A, l: L): WriterT[M, L, A] =
        WriterT.put(a)(l)
    }

}

object tell extends TellInstances
