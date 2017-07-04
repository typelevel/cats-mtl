package cats
package mtl
package instances

import cats.data.WriterT

trait TellInstances extends TellInstancesLowPriority1 {
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

private[instances] trait TellInstancesLowPriority1 {
  implicit final def tellWriter[M[_], L](implicit L: Monoid[L], M: Applicative[M]): FunctorTell[CurryT[WriterTCL[L]#l, M]#l, L] = {
    new FunctorTell[CurryT[WriterTCL[L]#l, M]#l, L] {
      val functor = WriterT.catsDataApplicativeForWriterT(M, L)

      def tell(l: L): WriterT[M, L, Unit] = WriterT.tell(l)

      def writer[A](a: A, l: L): WriterT[M, L, A] = WriterT.put(a)(l)

      def tuple[A](ta: (L, A)): WriterT[M, L, A] = WriterT(M.pure(ta))
    }
  }

  implicit final def tellTuple[L](implicit L: Monoid[L]): FunctorTell[TupleC[L]#l, L] = {
    new FunctorTell[TupleC[L]#l, L] {
      val functor = cats.instances.tuple.catsStdMonadForTuple2

      def tell(l: L): (L, Unit) = (l, ())

      def writer[A](a: A, l: L): (L, A) = (l, a)

      def tuple[A](ta: (L, A)): (L, A) = ta
    }
  }
}

object tell extends TellInstances
