package cats
package mtl
package instances

import data.{IndexedReaderWriterStateT, IndexedStateT, ReaderWriterStateT, StateT}

trait StateInstances extends StateInstancesLowPriority1 {
  // this dependency on LayerFunctor is required because non-`LayerFunctor`s may not be lawful
  // to lift MonadState into
  implicit final def stateInd[M[_], Inner[_], E](implicit ml: MonadLayerFunctor[M, Inner],
                                                 under: MonadState[Inner, E]
                                                    ): MonadState[M, E] = {
    new MonadState[M, E] {
      val monad: Monad[M] = ml.outerInstance

      def get: M[E] = ml.layer(under.get)

      def set(s: E): M[Unit] = ml.layer(under.set(s))

      def modify(f: E => E): M[Unit] = ml.layer(under.modify(f))

      def inspect[A](f: (E) => A): M[A] = ml.layer(under.inspect(f))
    }
  }

  implicit final def stateIdState[S]: MonadState[StateTC[Id, S]#l, S] = {
    stateState[Id, S]
  }
}

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
