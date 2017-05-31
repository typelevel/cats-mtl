package cats
package mtl
package instances

import cats.data.ReaderT
import cats.mtl.monad.Layer

trait ReaderTInstances extends ReaderTInstancesLowPriority {
  implicit final def readerMonadLayer[M[_], E](implicit M: Monad[M]): Layer[CurryT[ReaderTCE[E]#l, M]#l, M] =
    readerMonadTransControl[M, E]
}

trait ReaderTInstancesLowPriority {

  implicit final def readerMonadTransControl[M[_], E]
  (implicit M: Monad[M]): monad.TransFunctor.Aux[CurryT[ReaderTCE[E]#l, M]#l, M, ReaderTCE[E]#l] = {
    new monad.TransFunctor[CurryT[ReaderTCE[E]#l, M]#l, M] {
      type Outer[F[_], A] = ReaderTCE[E]#l[F, A]

      val outerMonad: Monad[CurryT[ReaderTCE[E]#l, M]#l] =
        ReaderT.catsDataMonadReaderForKleisli
      val innerMonad: Monad[M] = M

      def layerMapK[A](ma: ReaderT[M, E, A])(trans: M ~> M): ReaderT[M, E, A] = ma.transform(trans)

      def layer[A](inner: M[A]): ReaderT[M, E, A] = ReaderT.lift(inner)

      def showLayers[F[_], A](ma: F[ReaderT[M, E, A]]): F[ReaderT[M, E, A]] = ma

      def hideLayers[F[_], A](foia: F[ReaderT[M, E, A]]): F[ReaderT[M, E, A]] = foia

      def transMap[A, N[_], NInner[_]]
      (ma: ReaderT[M, E, A])(trans: M ~> NInner)
      (implicit mt: monad.Trans.Aux[N, NInner, ReaderTCE[E]#l]): N[A] = {
        mt.hideLayers[Id, A](ma.transform(trans))
      }
    }
  }

}

object readert extends ReaderTInstances
