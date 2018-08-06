package cats
package mtl
package instances

import cats.data.{IndexedReaderWriterStateT, Kleisli, ReaderT, ReaderWriterStateT}
import cats.mtl.lifting.MonadLayer

trait LocalInstances extends LocalLowPriorityInstances {
  implicit final def localInd[M[_], Inner[_], E](implicit ml: MonadLayer[M, Inner],
                                                 under: ApplicativeLocal[Inner, E]
                                                ): ApplicativeLocal[M, E] = {
    new ApplicativeLocal[M, E] {
      val ask: ApplicativeAsk[M, E] =
        instances.ask.askLayerInd[M, Inner, E](ml, under.ask)

      def local[A](f: E => E)(fa: M[A]): M[A] = {
        ml.outerInstance.flatMap(ask.ask)(r =>
          ml.layerImapK(fa)(new (Inner ~> Inner) {
            def apply[X](fa: Inner[X]): Inner[X] = under.local(f)(fa)
          }, new (Inner ~> Inner) {
            def apply[X](fa: Inner[X]): Inner[X] = under.scope(r)(fa)
          }))
      }

      def scope[A](e: E)(fa: M[A]): M[A] = {
        ml.outerInstance.flatMap(ask.ask)(r =>
          ml.layerImapK(fa)(new (Inner ~> Inner) {
            def apply[X](fa: Inner[X]): Inner[X] = under.scope(e)(fa)
          }, new (Inner ~> Inner) {
            def apply[X](fa: Inner[X]): Inner[X] = under.scope(r)(fa)
          }))
      }

    }
  }

  implicit final def localReaderId[E]: ApplicativeLocal[ReaderTC[Id, E]#l, E] = {
    localReader[Id, E]
  }

}

private[instances] trait LocalLowPriorityInstances {
  implicit final def localReader[M[_], E](implicit M: Applicative[M]): ApplicativeLocal[ReaderTC[M, E]#l, E] = {
    new ApplicativeLocal[ReaderTC[M, E]#l, E] {
      val ask: ApplicativeAsk[ReaderTC[M, E]#l, E] =
        askReader[M, E]

      def local[A](f: E => E)(fa: ReaderT[M, E, A]): ReaderT[M, E, A] = ReaderT.local(f)(fa)

      def scope[A](e: E)(fa: ReaderT[M, E, A]): ReaderT[M, E, A] = ReaderT(_ => fa.run(e))
    }
  }

  implicit final def localFunction[E]: ApplicativeLocal[FunctionC[E]#l, E] = {
    new ApplicativeLocal[FunctionC[E]#l, E] {
      val ask: ApplicativeAsk[FunctionC[E]#l, E] =
        askFunction[E]

      def local[A](f: E => E)(fa: E => A): E => A = fa.compose(f)

      def scope[A](e: E)(fa: E => A): E => A = _ => fa(e)
    }
  }

  implicit final def localReaderWriterState[M[_], E, L, S]
  (implicit M: Monad[M], L: Monoid[L]): ApplicativeLocal[ReaderWriterStateT[M, E, L, S, ?], E] =
    new DefaultApplicativeLocal[ReaderWriterStateT[M, E, L, S, ?], E] {
      val ask: ApplicativeAsk[ReaderWriterStateT[M, E, L, S, ?], E] =
        askReaderWriterState[M, E, L, S]

      def local[A](f: E => E)(fa: ReaderWriterStateT[M, E, L, S, A]): ReaderWriterStateT[M, E, L, S, A] =
        ReaderWriterStateT((e, s) => fa.run(f(e), s))
    }

  final def askReader[M[_], E](implicit M: Applicative[M]): ApplicativeAsk[ReaderTC[M, E]#l, E] = {
    new ApplicativeAsk[ReaderTC[M, E]#l, E] {
      val applicative: Applicative[ReaderTC[M, E]#l] =
        ReaderT.catsDataApplicativeForKleisli(M)

      def ask: ReaderT[M, E, E] = Kleisli.ask[M, E]

      def reader[A](f: E => A): ReaderT[M, E, A] = Kleisli(a => M.pure(f(a)))
    }
  }

  final def askFunction[E]: ApplicativeAsk[FunctionC[E]#l, E] = {
    new ApplicativeAsk[FunctionC[E]#l, E] {
      val applicative: Applicative[FunctionC[E]#l] =
        cats.instances.function.catsStdMonadForFunction1[E]

      def ask: E => E = identity[E]

      def reader[A](f: E => A): E => A = f
    }
  }

  final def askReaderWriterState[M[_], E, L, S]
  (implicit M: Monad[M], L: Monoid[L]): ApplicativeAsk[ReaderWriterStateT[M, E, L, S, ?], E] = {
    new DefaultApplicativeAsk[ReaderWriterStateT[M, E, L, S, ?], E] {
      val applicative: Applicative[ReaderWriterStateT[M, E, L, S, ?]] =
        IndexedReaderWriterStateT.catsDataMonadForRWST

      def ask: ReaderWriterStateT[M, E, L, S, E] = ReaderWriterStateT.ask[M, E, L, S]
    }
  }
}

object local extends LocalInstances
