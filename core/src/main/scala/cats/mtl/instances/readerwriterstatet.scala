package cats
package mtl
package instances

import cats.data.{IndexedReaderWriterStateT, ReaderWriterStateT}
import cats.mtl.lifting.MonadLayerControl
import cats.syntax.functor._

trait ReaderWriterStateTInstances {

  implicit final def readerWriterStateMonadLayerControl[M[_], E, L, S]
  (implicit M: Monad[M], L: Monoid[L]): MonadLayerControl.Aux[ReaderWriterStateT[M, E, L, S, ?], M, (L, S, ?)] = {
    new MonadLayerControl[ReaderWriterStateT[M, E, L, S, ?], M] {
      type State[A] = (L, S, A)

      val outerInstance: Monad[ReaderWriterStateT[M, E, L, S, ?]] =
        IndexedReaderWriterStateT.catsDataMonadForRWST

      val innerInstance: Monad[M] = M

      def layerMapK[A](ma: ReaderWriterStateT[M, E, L, S, A])(trans: M ~> M): ReaderWriterStateT[M, E, L, S, A] =
        ma.transformF(trans(_))

      def layer[A](inner: M[A]): ReaderWriterStateT[M, E, L, S, A] =
        ReaderWriterStateT.liftF(inner)

      def restore[A](state: (L, S, A)): ReaderWriterStateT[M, E, L, S, A] =
        ReaderWriterStateT((e, s) => innerInstance.pure(state))

      def layerControl[A](cps: (ReaderWriterStateT[M, E, L, S, ?] ~> (M of (L, S, ?))#l) => M[A]): ReaderWriterStateT[M, E, L, S, A] = {
        ReaderWriterStateT((e: E, s: S) => cps(new (ReaderWriterStateT[M, E, L, S, ?] ~> (M of (L, S, ?))#l) {
          def apply[X](fa: ReaderWriterStateT[M, E, L, S, X]): M[(L, S, X)] = fa.run(e, s)
        }).map(x => (L.empty, s, x)))
      }

      def zero[A](state: (L, S, A)): Boolean = false
    }
  }
}

object readerwriterstatet extends ReaderWriterStateTInstances
