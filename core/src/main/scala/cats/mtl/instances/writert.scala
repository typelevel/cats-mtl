package cats
package mtl
package instances

import cats.data.WriterT

trait WriterTInstances extends WriterTInstancesLowPriority {
  implicit final def writerMonadLayer[M[_], L]
  (implicit L: Monoid[L], M: Monad[M]): monad.Layer[WriterTC[M, L]#l, M] = {
    writerMonadTransControl[M, L]
  }
}

private[instances] trait WriterTInstancesLowPriority {
  implicit final def writerMonadTransControl[M[_], L]
  (implicit L: Monoid[L], M: Monad[M]): monad.TransFunctor.Aux[WriterTC[M, L]#l, M, WriterTCL[L]#l] = {
    new monad.TransFunctor[CurryT[WriterTCL[L]#l, M]#l, M] {
      type Outer[F[_], A] = WriterT[F, L, A]

      val outerInstance: Monad[CurryT[WriterTCL[L]#l, M]#l] =
        WriterT.catsDataMonadWriterForWriterT

      val innerInstance: Monad[M] = M

      def layerMapK[A](ma: WriterT[M, L, A])(trans: M ~> M): WriterT[M, L, A] = WriterT(trans(ma.run))

      def layer[A](inner: M[A]): WriterT[M, L, A] = WriterT.lift(inner)

      def showLayers[F[_], A](ma: F[WriterT[M, L, A]]): F[WriterT[M, L, A]] = ma

      def hideLayers[F[_], A](foia: F[WriterT[M, L, A]]): F[WriterT[M, L, A]] = foia

      def transMap[A, N[_], NInner[_]]
      (ma: WriterT[M, L, A])(trans: M ~> NInner)
      (implicit mt: monad.Trans.Aux[N, NInner, WriterTCL[L]#l]): N[A] = {
        mt.hideLayers[Id, A](WriterT(trans(ma.run)))
      }

    }
  }
}

object writert extends WriterTInstances

