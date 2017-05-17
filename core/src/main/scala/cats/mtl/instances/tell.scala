package cats
package mtl
package instances

import cats.data.WriterT

trait TellInstances extends TellInstancesLowPriority {
  implicit def tellInd[M[_], Inner[_], L](implicit
                                          lift: MonadLayer.Aux[M, Inner],
                                          under: Tell[Inner, L]
                                         ): Tell[M, L] =
    new Tell[M, L] {
      val monad = lift.monad

      def tell(l: L): M[Unit] = lift.layer(under.tell(l))

      def writer[A](a: A, l: L): M[A] = lift.layer(under.writer(a, l))
    }
}

trait TellInstancesLowPriority {

  implicit def tellWriter[M[_], L](implicit L: Monoid[L], M: Monad[M]): Tell[CurryT[WriterTCL[L]#l, M]#l, L] =
    new Tell[CurryT[WriterTCL[L]#l, M]#l, L] {

      val monad =
        WriterT.catsDataMonadWriterForWriterT(M, L)

      def tell(l: L): WriterT[M, L, Unit] =
        WriterT.tell(l)

      def writer[A](a: A, l: L): WriterT[M, L, A] =
        WriterT.put(a)(l)
    }

}

object tell extends TellInstances
