package cats
package mtl
package instances

import cats.data.OptionT
import cats.mtl.monad.Layer

trait OptionTInstances extends OptionTInstancesLowPriority {
  implicit final def optionMonadLayer[M[_]](implicit M: Monad[M]): Layer[OptionTC[M]#l, M] =
    optionMonadTransFunctor[M]
}

trait OptionTInstancesLowPriority {

  implicit final def optionMonadTransFunctor[M[_]]
  (implicit M: Monad[M]): monad.TransFunctor.Aux[OptionTC[M]#l, M, OptionT] = {
    new monad.TransFunctor[OptionTC[M]#l, M] {
      type Outer[F[_], A] = OptionT[F, A]

      val outerMonad: Monad[OptionTC[M]#l] =
        OptionT.catsDataMonadForOptionT
      val innerMonad: Monad[M] = M

      def layerMapK[A](ma: OptionT[M, A])(trans: M ~> M): OptionT[M, A] = OptionT(trans(ma.value))

      def layer[A](inner: M[A]): OptionT[M, A] = OptionT.liftF(inner)

      def showLayers[F[_], A](ma: F[OptionT[M, A]]): F[OptionT[M, A]] = ma

      def hideLayers[F[_], A](foia: F[OptionT[M, A]]): F[OptionT[M, A]] = foia

      def transMap[A, N[_], NInner[_]]
      (ma: OptionT[M, A])(trans: M ~> NInner)
      (implicit mt: monad.Trans.AuxIO[N, NInner, OptionT]): N[A] = {
        mt.hideLayers[Id, A](OptionT(trans(ma.value)))
      }

    }
  }
}

object optiont {

}
