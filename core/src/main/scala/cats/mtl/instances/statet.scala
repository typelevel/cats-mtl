package cats
package mtl
package instances

import cats.data.StateT
import cats.mtl.monad.Layer

trait StateTInstances extends StateTInstancesLowPriority {
  implicit final def stateMonadLayer[M[_], S](implicit M: Monad[M]): Layer[StateTC[M, S]#l, M] =
    stateMonadTransControl[M, S]
}

private[instances] trait StateTInstancesLowPriority {
  implicit final def stateMonadTransControl[M[_], S]
  (implicit M: Monad[M]): monad.TransFunctor.Aux[CurryT[StateTCS[S]#l, M]#l, M, StateTCS[S]#l] =
    new monad.TransFunctor[StateTC[M, S]#l, M] {

      type Outer[F[_], A] = StateT[F, S, A]

      val outerMonad: Monad[StateTC[M, S]#l] =
        StateT.catsDataMonadForStateT
      val innerMonad: Monad[M] = M

      def layerMapK[A](ma: StateT[M, S, A])(trans: M ~> M): StateT[M, S, A] = ma.transformF(trans(_))

      def layer[A](inner: M[A]): StateT[M, S, A] = StateT.lift(inner)

      def showLayers[F[_], A](ma: F[StateT[M, S, A]]): F[StateT[M, S, A]] = ma

      def hideLayers[F[_], A](foia: F[StateT[M, S, A]]): F[StateT[M, S, A]] = foia

      def transMap[A, N[_], NInner[_]]
      (ma: StateT[M, S, A])(trans: M ~> NInner)
      (implicit mt: monad.Trans.Aux[N, NInner, StateTCS[S]#l]): N[A] = {
        mt.hideLayers[Id, A](ma.transformF(trans(_))(innerMonad, mt.innerMonad))
      }
    }

}

object statet extends StateTInstances
