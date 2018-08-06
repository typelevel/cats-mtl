package cats
package mtl
package instances

import cats.data.{IndexedStateT, StateT}
import cats.mtl.lifting.MonadLayerControl
import cats.syntax.functor._

trait StateTInstances {
  implicit final def stateMonadLayerControl[M[_], S]
  (implicit M: Monad[M]): MonadLayerControl.Aux[StateTC[M, S]#l, M, TupleC[S]#l] = {
    new MonadLayerControl[StateTC[M, S]#l, M] {
      type State[A] = (S, A)

      val outerInstance: Monad[StateTC[M, S]#l] =
        IndexedStateT.catsDataMonadForIndexedStateT

      val innerInstance: Monad[M] = M

      def layerMapK[A](ma: StateT[M, S, A])(trans: M ~> M): StateT[M, S, A] = ma.transformF(trans(_))

      def layer[A](inner: M[A]): StateT[M, S, A] = StateT.liftF(inner)

      def restore[A](state: (S, A)): StateT[M, S, A] = StateT((_: S) => innerInstance.pure(state))

      def layerControl[A](cps: (StateTC[M, S]#l ~> (M of TupleC[S]#l)#l) => M[A]): StateT[M, S, A] = {
        StateT((s: S) => cps(new (StateTC[M, S]#l ~> (M of TupleC[S]#l)#l) {
          def apply[X](fa: StateT[M, S, X]): M[(S, X)] = fa.run(s)
        }).map(x => (s, x)))
      }

      def zero[A](state: (S, A)): Boolean = false
    }
  }
}

object statet extends StateTInstances
