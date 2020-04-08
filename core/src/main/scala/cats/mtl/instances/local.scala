package cats
package mtl
package instances

import cats.data.{IndexedReaderWriterStateT, Kleisli, ReaderT, ReaderWriterStateT}

trait LocalInstances extends LocalLowPriorityInstances

private[instances] trait LocalLowPriorityInstances {
  implicit final def localReader[M[_], E](implicit M: Applicative[M]): ApplicativeLocal[ReaderTC[M, E]#l, E] = {
    new ApplicativeLocal[ReaderTC[M, E]#l, E] {
      val applicative: Applicative[ReaderTC[M, E]#l] =
        ReaderT.catsDataApplicativeForKleisli(M)

      def ask: ReaderT[M, E, E] = Kleisli.ask[M, E]

      override def reader[A](f: E => A): ReaderT[M, E, A] = Kleisli(a => M.pure(f(a)))

      def local[A](f: E => E)(fa: ReaderT[M, E, A]): ReaderT[M, E, A] = ReaderT.local(f)(fa)

      override def scope[A](e: E)(fa: ReaderT[M, E, A]): ReaderT[M, E, A] = ReaderT(_ => fa.run(e))
    }
  }

  implicit final def localFunction[E]: ApplicativeLocal[FunctionC[E]#l, E] = {
    new ApplicativeLocal[FunctionC[E]#l, E] {
      val applicative: Applicative[FunctionC[E]#l] =
        cats.instances.function.catsStdMonadForFunction1[E]

      def ask: E => E = identity[E]

      override def reader[A](f: E => A): E => A = f

      def local[A](f: E => E)(fa: E => A): E => A = fa.compose(f)

      override def scope[A](e: E)(fa: E => A): E => A = _ => fa(e)
    }
  }

  implicit final def localReaderWriterState[M[_], E, L, S]
  (implicit M: Monad[M], L: Monoid[L]): ApplicativeLocal[ReaderWriterStateT[M, E, L, S, ?], E] =
    new ApplicativeLocal[ReaderWriterStateT[M, E, L, S, ?], E] {
      val applicative: Applicative[ReaderWriterStateT[M, E, L, S, ?]] =
        IndexedReaderWriterStateT.catsDataMonadForRWST

      def ask: ReaderWriterStateT[M, E, L, S, E] = ReaderWriterStateT.ask[M, E, L, S]

      def local[A](f: E => E)(fa: ReaderWriterStateT[M, E, L, S, A]): ReaderWriterStateT[M, E, L, S, A] =
        ReaderWriterStateT((e, s) => fa.run(f(e), s))
    }
}

object local extends LocalInstances
