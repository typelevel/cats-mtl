package cats
package mtl
package instances

import cats.data.EitherT

object either {
  def eitherMonadTransControl[M[_], E]
  (implicit M: Monad[M]): MonadTransControl[CurryT[EitherTCE[E]#l, M]#l] = {
    new MonadTransControl[CurryT[EitherTCE[E]#l, M]#l] {
      type State[A] = E Either A
      type Inner[A] = M[A]
      type Outer[F[_], A] = EitherT[F, E, A]

      def restore[A](state: Either[E, A]): EitherT[M, E, A] = EitherT.fromEither(state)

      def zero[A](state: Either[E, A]): Boolean = state.isLeft

      val monad: Monad[CurryT[EitherTCE[E]#l, M]#l] =
        EitherT.catsDataMonadErrorForEitherT
      val innerMonad: Monad[M] = M

      def transControl[A](cps: MonadTransContinuation[State, Outer, A]): EitherT[M, E, A] = {
        EitherT.right[M, E, A](
          cps(new (EitherTC[M, E]#l ~> (M of EitherC[E]#l)#l) {
            def apply[X](fa: EitherT[M, E, X]): M[Either[E, X]] = fa.value
          })(this)
        )
      }

      def layerControl[A](cps: (EitherTC[M, E]#l ~> (M of EitherC[E]#l)#l) => M[A]): EitherT[M, E, A] = {
        EitherT.right[M, E, A](
          cps(new (EitherTC[M, E]#l ~> (M of EitherC[E]#l)#l) {
            def apply[X](fa: EitherT[M, E, X]): M[Either[E, X]] = fa.value
          })
        )
      }

      def layerMap[A](ma: EitherT[M, E, A])(trans: M ~> M): EitherT[M, E, A] = EitherT(trans(ma.value))

      def layer[A](inner: M[A]): EitherT[M, E, A] = EitherT.right(inner)

      def imapK[A](ma: EitherT[M, E, A])(forward: M ~> M, backward: M ~> M): EitherT[M, E, A] = layerMap(ma)(forward)

      def showLayers[F[_], A](ma: F[EitherT[M, E, A]]): F[EitherT[M, E, A]] = ma

      def hideLayers[F[_], A](foia: F[EitherT[M, E, A]]): F[EitherT[M, E, A]] = foia

      def transInvMap[N[_], NInner[_], A]
      (ma: EitherT[M, E, A])(forward: M ~> NInner, backward: NInner ~> M)
      (implicit other: MonadTrans.AuxIO[N, NInner, EitherTCE[E]#l]): N[A] = {
        transMap(ma)(forward)
      }

      def transMap[A, N[_], NInner[_]]
      (ma: EitherT[M, E, A])(trans: M ~> NInner)
      (implicit mt: MonadTrans.AuxIO[N, NInner, EitherTCE[E]#l]): N[A] = {
        mt.hideLayers[Id, A](EitherT(trans(ma.value)))
      }

    }
  }
}
