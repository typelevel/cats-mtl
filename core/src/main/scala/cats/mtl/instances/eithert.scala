package cats
package mtl
package instances

import cats.data.EitherT

object eithert {
  def eitherMonadTransControl[M[_], E]
  (implicit M: Monad[M]): monad.TransFunctor.Aux[EitherTC[M, E]#l, M, EitherTCE[E]#l] = {
    new monad.TransFunctor[EitherTC[M, E]#l, M] {
      type Outer[F[_], A] = EitherT[F, E, A]

      val outerMonad: Monad[CurryT[EitherTCE[E]#l, M]#l] =
        EitherT.catsDataMonadErrorForEitherT
      val innerMonad: Monad[M] = M

      def layerMapK[A](ma: EitherT[M, E, A])(trans: M ~> M): EitherT[M, E, A] = EitherT(trans(ma.value))

      def layer[A](inner: M[A]): EitherT[M, E, A] = EitherT.right(inner)

      def showLayers[F[_], A](ma: F[EitherT[M, E, A]]): F[EitherT[M, E, A]] = ma

      def hideLayers[F[_], A](foia: F[EitherT[M, E, A]]): F[EitherT[M, E, A]] = foia

      def transMap[A, N[_], NInner[_]]
      (ma: EitherT[M, E, A])(trans: M ~> NInner)
      (implicit mt: monad.Trans.AuxIO[N, NInner, EitherTCE[E]#l]): N[A] = {
        mt.hideLayers[Id, A](EitherT(trans(ma.value)))
      }

    }
  }
}
