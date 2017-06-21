package cats
package mtl
package instances

import cats.data.StateT
import cats.syntax.functor._

trait StateTInstances extends StateTInstancesLowPriority {
}

private[instances] trait StateTInstancesLowPriority {
  implicit final def stateMonadLayerControl[M[_], S]
  (implicit M: Monad[M]): monad.LayerControl.Aux[CurryT[StateTCS[S]#l, M]#l, M, TupleC[S]#l] = {
    new monad.LayerControl[StateTC[M, S]#l, M] {
      type State[A] = (S, A)

      val outerInstance: Monad[StateTC[M, S]#l] =
        StateT.catsDataMonadForStateT

      val innerInstance: Monad[M] = M

      def layerMapK[A](ma: StateT[M, S, A])(trans: M ~> M): StateT[M, S, A] = ma.transformF(trans(_))

      def layer[A](inner: M[A]): StateT[M, S, A] = StateT.lift(inner)

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
