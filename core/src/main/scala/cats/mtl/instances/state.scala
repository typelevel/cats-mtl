package cats
package mtl
package instances

import data.{IndexedReaderWriterStateT, IndexedStateT, ReaderWriterStateT, StateT}

trait StateInstances extends StateInstancesLowPriority1

private[instances] trait StateInstancesLowPriority1 {
  implicit final def stateState[M[_], S](implicit M: Monad[M]): MonadState[StateTC[M, S]#l, S] = {
    new MonadState[StateTC[M, S]#l, S] {
      val monad: Monad[StateTC[M, S]#l] = IndexedStateT.catsDataMonadForIndexedStateT

      def get: StateT[M, S, S] = StateT.get

      def set(s: S): StateT[M, S, Unit] = StateT.set(s)

      def modify(f: S => S): StateT[M, S, Unit] = StateT.modify(f)

      def inspect[A](f: (S) => A): StateT[M, S, A] = StateT.inspect(f)
    }
  }

  implicit final def readerWriterStateState[M[_], R, L, S]
  (implicit M: Monad[M], L: Monoid[L]): MonadState[ReaderWriterStateT[M, R, L, S, ?], S] =
    new MonadState[ReaderWriterStateT[M, R, L, S, ?], S] {
      val monad: Monad[ReaderWriterStateT[M, R, L, S, ?]] = IndexedReaderWriterStateT.catsDataMonadForRWST

      def get: ReaderWriterStateT[M, R, L, S, S] =
        ReaderWriterStateT.get[M, R, L, S]

      def set(s: S): ReaderWriterStateT[M, R, L, S, Unit] =
        ReaderWriterStateT.set[M, R, L, S](s)

      def modify(f: S => S): ReaderWriterStateT[M, R, L, S, Unit] =
        ReaderWriterStateT.modify[M, R, L, S](f)

      def inspect[A](f: S => A): ReaderWriterStateT[M, R, L, S, A] =
        ReaderWriterStateT.inspect[M, R, L, S, A](f)
    }
}

object state extends StateInstances
