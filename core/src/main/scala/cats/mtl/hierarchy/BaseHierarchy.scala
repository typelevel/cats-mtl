package cats
package mtl
package hierarchy

trait BaseHierarchy extends BH0

object base extends BaseHierarchy

private[hierarchy] trait BH0 extends BH1 {
  implicit final def functorEmptyFromTraverseEmpty[F[_]](implicit F: TraverseEmpty[F]): FunctorEmpty[F] = F.functorEmpty
}

private[hierarchy] trait BH1 {
  implicit final def tellFromState[F[_], L](implicit state: MonadState[F, L]): FunctorTell[F, L] = {
    new FunctorTell[F, L] {
      override val functor: Functor[F] = state.monad

      override def tell(l: L): F[Unit] = state.set(l)

      override def writer[A](a: A, l: L): F[A] = functor.as(state.set(l), a)

      override def tuple[A](ta: (L, A)): F[A] = writer(ta._2, ta._1)
    }
  }

  implicit final def askFromState[F[_], L](implicit state: MonadState[F, L]): ApplicativeAsk[F, L] = {
    new ApplicativeAsk[F, L] {
      override val applicative: Applicative[F] = state.monad

      override def ask: F[L] = state.get

      override def reader[A](f: (L) => A): F[A] = state.inspect(f)
    }
  }
}
