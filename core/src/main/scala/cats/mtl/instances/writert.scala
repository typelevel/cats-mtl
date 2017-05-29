package cats
package mtl
package instances

import cats.data.WriterT
import cats.mtl.monad.{Layer, TransFunctor}

trait WriterTInstances extends WriterTInstancesLowPriority {
  implicit final def writerMonadLayer[M[_], L](implicit L: Monoid[L], M: Monad[M]): Layer[CurryT[WriterTCL[L]#l, M]#l, M] =
    writerMonadTransControl[M, L]
}

private[instances] trait WriterTInstancesLowPriority {
  implicit final def writerMonadTransControl[M[_], L]
  (implicit L: Monoid[L], M: Monad[M]): TransFunctor.Aux[CurryT[WriterTCL[L]#l, M]#l, M, WriterTCL[L]#l] = {
    new TransFunctor[CurryT[WriterTCL[L]#l, M]#l, M] {
      import cats.mtl.monad._
      type State[A] = (L, A)

      type Inner[A] = M[A]

      type Outer[F[_], A] = WriterT[F, L, A]

      def restore[A](state: (L, A)): WriterT[M, L, A] = WriterT(M.pure(state))

      def zero[A](state: (L, A)): Boolean = false

      val monad: Monad[CurryT[WriterTCL[L]#l, M]#l] =
        WriterT.catsDataMonadWriterForWriterT
      val innerMonad: Monad[M] = M

      def layerMapK[A](ma: WriterT[M, L, A])(trans: M ~> M): WriterT[M, L, A] = WriterT(trans(ma.run))

      def layer[A](inner: M[A]): WriterT[M, L, A] = WriterT.lift(inner)

      def showLayers[F[_], A](ma: F[WriterT[M, L, A]]): F[WriterT[M, L, A]] = ma

      def hideLayers[F[_], A](foia: F[WriterT[M, L, A]]): F[WriterT[M, L, A]] = foia

      def transInvMap[N[_], NInner[_], A]
      (ma: WriterT[M, L, A])(forward: M ~> NInner, backward: NInner ~> M)
      (implicit other: Trans.AuxIO[N, NInner, WriterTCL[L]#l]): N[A] = {
        transMap(ma)(forward)
      }

      def transMap[A, N[_], NInner[_]]
      (ma: WriterT[M, L, A])(trans: M ~> NInner)
      (implicit mt: Trans.AuxIO[N, NInner, WriterTCL[L]#l]): N[A] = {
        mt.hideLayers[Id, A](WriterT(trans(ma.run)))
      }

    }
  }
}

object writert extends WriterTInstances {

}
