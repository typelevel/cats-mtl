package cats
package mtl
package instances

import cats.data.StateT
import cats.syntax.all._

trait StateTInstances extends StateTInstancesLowPriority {
  implicit final def stateMonadLayer[M[_], S](implicit M: Monad[M]): MonadLayer[StateTC[M, S]#l, M] =
    stateMonadTransControl[M, S]
}

private[instances] trait StateTInstancesLowPriority {
  implicit final def stateMonadTransControl[M[_], S]
  (implicit M: Monad[M]): MonadTransControl.Aux[CurryT[StateTCS[S]#l, M]#l, TupleC[S]#l, M, StateTCS[S]#l] =
    new MonadTransControl[StateTC[M, S]#l, M] {

      type State[A] = (S, A)

      type Inner[A] = M[A]

      type Outer[F[_], A] = StateT[F, S, A]

      def restore[A](state: (S, A)): StateT[M, S, A] =
        StateT((_: S) => M.pure(state))

      def zero[A](state: (S, A)): Boolean = false

      val monad: Monad[StateTC[M, S]#l] =
        StateT.catsDataMonadForStateT
      val innerMonad: Monad[M] = M

      def transControl[A](cps: MonadTransContinuation[State, Outer, A]): StateT[M, S, A] = {
        StateT[M, S, A](s =>
          cps(new (StateTC[M, S]#l ~> (M of (TupleC[S]#l))#l) {
            def apply[X](fa: StateT[M, S, X]): M[(S, X)] = fa.run(s)
          })(this).map(a => (s, a))
        )
      }

      def layerControl[A](cps: (StateTC[M, S]#l ~> (M of TupleC[S]#l)#l) => M[A]): StateT[M, S, A] = {
        StateT[M, S, A](s =>
          cps(new (StateTC[M, S]#l ~> (M of TupleC[S]#l)#l) {
            def apply[X](fa: StateT[M, S, X]): M[(S, X)] = fa.run(s)
          }).map(a => (s, a))
        )
      }

      def layerMap[A](ma: StateT[M, S, A])(trans: M ~> M): StateT[M, S, A] = ma.transformF(trans(_))

      def layer[A](inner: M[A]): StateT[M, S, A] = StateT.lift(inner)

      def imapK[A](ma: StateT[M, S, A])(forward: M ~> M, backward: M ~> M): StateT[M, S, A] = layerMap(ma)(forward)

      def showLayers[F[_], A](ma: F[StateT[M, S, A]]): F[StateT[M, S, A]] = ma

      def hideLayers[F[_], A](foia: F[StateT[M, S, A]]): F[StateT[M, S, A]] = foia

      def transInvMap[N[_], NInner[_], A]
      (ma: StateT[M, S, A])(forward: M ~> NInner, backward: NInner ~> M)
      (implicit other: MonadTrans.AuxIO[N, NInner, StateTCS[S]#l]): N[A] = {
        transMap(ma)(forward)
      }

      def transMap[A, N[_], NInner[_]]
      (ma: StateT[M, S, A])(trans: M ~> NInner)
      (implicit mt: MonadTrans.AuxIO[N, NInner, StateTCS[S]#l]): N[A] = {
        mt.hideLayers[Id, A](ma.transformF(trans(_))(innerMonad, mt.innerMonad))
      }
    }

}

object statet extends StateTInstances
