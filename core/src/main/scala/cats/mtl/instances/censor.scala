package cats
package mtl
package instances

import cats.data.{IndexedReaderWriterStateT, ReaderWriterStateT, WriterT}
import cats.syntax.functor._

trait CensorInstances extends CensorInstancesLowPriority {
  implicit final def passWriterId[L](implicit L: Monoid[L]): ApplicativeCensor[WriterTC[Id, L]#l, L] =
    passWriter[Id, L]
}

trait CensorInstancesLowPriority {
  implicit final def passWriter[M[_], L](implicit M: Monad[M], L: Monoid[L]): ApplicativeCensor[WriterT[M, L, ?], L] =
    new ApplicativeCensor[WriterT[M, L, ?], L] {
      val applicative: Applicative[WriterT[M, L, ?]] =
        cats.data.WriterT.catsDataMonadForWriterT[M, L]

      val monoid: Monoid[L] = L

      def clear[A](fa: WriterT[M, L, A]): WriterT[M, L, A] =
        WriterT(fa.value.tupleLeft(L.empty))

      def censor[A](fa: WriterT[M, L, A])(f: L => L): WriterT[M, L, A] =
        WriterT(fa.run.map { case (l, a) => (f(l), a) })

      def tell(l: L): WriterT[M, L, Unit] = WriterT.tell(l)

      def writer[A](a: A, l: L): WriterT[M, L, A] = WriterT.put(a)(l)

      def tuple[A](ta: (L, A)): WriterT[M, L, A] = WriterT(M.pure(ta))

      def listen[A](fa: WriterT[M, L, A]): WriterT[M, L, (A, L)] =
        WriterT[M, L, (A, L)](fa.run.map { case (l, a) => (l, (a, l)) })

      def listens[A, B](fa: WriterT[M, L, A])(f: (L) => B): WriterT[M, L, (A, B)] =
        WriterT[M, L, (A, B)](fa.run.map { case (l, a) => (l, (a, f(l))) })
    }

  implicit final def passTuple[L](implicit L: Monoid[L]): ApplicativeCensor[(L, ?), L] =
    new ApplicativeCensor[(L, ?), L] {
      val applicative: Applicative[Tuple2[L, ?]] =
        cats.instances.tuple.catsStdMonadForTuple2

      val monoid: Monoid[L] = L

      def tell(l: L): (L, Unit) = (l, ())

      def writer[A](a: A, l: L): (L, A) = (l, a)

      def tuple[A](ta: (L, A)): (L, A) = ta

      def clear[A](fa: (L, A)): (L, A) = (L.empty, fa._2)

      def censor[A](fa: (L, A))(f: L => L): (L, A) = {
        val (l, a) = fa
        (f(l), a)
      }

      def listen[A](fa: (L, A)): (L, (A, L)) = {
        val (l, a) = fa
        (l, (a, l))
      }

      def listens[A, B](fa: (L, A))(f: (L) => B): (L, (A, B)) = {
        val (l, a) = fa
        (l, (a, f(l)))
      }
    }

  implicit final def passReaderWriterState[M[_], R, L, S](implicit L: Monoid[L], M: Monad[M]): ApplicativeCensor[ReaderWriterStateT[M, R, L, S, ?], L] =
    new DefaultApplicativeCensor[ReaderWriterStateT[M, R, L, S, ?], L] {
      val applicative: Applicative[ReaderWriterStateT[M, R, L, S, ?]] =
        IndexedReaderWriterStateT.catsDataMonadForRWST

      val monoid: Monoid[L] = L

      def tell(l: L): ReaderWriterStateT[M, R, L, S, Unit] = ReaderWriterStateT.tell(l)

      def listen[A](fa: ReaderWriterStateT[M, R, L, S, A]): ReaderWriterStateT[M, R, L, S, (A, L)] =
        ReaderWriterStateT((e, s) => fa.run(e, s).map {
          case (l, s, a) => (l, s, (a, l))
        })

      def censor[A](faf: ReaderWriterStateT[M, R, L, S, A])(f: L => L): ReaderWriterStateT[M, R, L, S, A] =
        ReaderWriterStateT((e, s) => faf.run(e, s).map {
          case (l, s, a) => (f(l), s, a)
        })

    }
}

object censor extends CensorInstances
