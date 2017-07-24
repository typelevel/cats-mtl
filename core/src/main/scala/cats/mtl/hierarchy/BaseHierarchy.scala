package cats
package mtl
package hierarchy

object BaseHierarchy extends BH0 {

}

private[hierarchy] trait BH0 {
  implicit final def askFromLocal[F[_], E](implicit local: ApplicativeLocal[F, E]): ApplicativeAsk[F, E] = local.ask

  implicit final def tellFromListen[F[_], L](implicit listen: FunctorListen[F, L]): FunctorTell[F, L] = listen.tell

  implicit final def tellFromState[F[_], L](implicit state: MonadState[F, L]): FunctorTell[F, L] = {
    new FunctorTell[F, L] {
      override val functor: Functor[F] = state.monadInstance

      override def tell(l: L): F[Unit] = state.set(l)

      override def writer[A](a: A, l: L): F[A] = functor.as(state.set(l), a)

      override def tuple[A](ta: (L, A)): F[A] = writer(ta._2, ta._1)
    }
  }

  implicit final def askFromState[F[_], L](implicit state: MonadState[F, L]): ApplicativeAsk[F, L] = {
    new ApplicativeAsk[F, L] {
      override val applicative: Applicative[F] = state.monadInstance

      override def ask: F[L] = state.get

      override def reader[A](f: (L) => A): F[A] = state.inspect(f)
    }
  }
}

