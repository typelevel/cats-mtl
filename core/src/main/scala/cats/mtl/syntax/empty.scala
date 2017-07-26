package cats
package mtl
package syntax

trait EmptySyntax {
  implicit def toEmptyOps[F[_], A](fa: F[A]): EmptyOps[F, A] = new EmptyOps[F, A](fa)
  implicit def toFlattenOptionOps[F[_], A](fa: F[Option[A]]): FlattenOptionOps[F, A] = new FlattenOptionOps[F, A](fa)
}

final class EmptyOps[F[_], A](val fa: F[A]) extends AnyVal {
  def traverseFilter[G[_] : Applicative, B](f: A => G[Option[B]])(implicit F: TraverseEmpty[F]): G[F[B]] = F.traverseFilter(fa)(f)

  def filterA[G[_]: Applicative](f: A => G[Boolean])(implicit F: TraverseEmpty[F]): G[F[A]] = F.filterA(fa)(f)

  def mapFilter[B](f: A => Option[B])(implicit F: FunctorEmpty[F]): F[B] = F.mapFilter(fa)(f)

  def collect[B](f: PartialFunction[A, B])(implicit F: FunctorEmpty[F]): F[B] = F.collect(fa)(f)

  def filter(f: A => Boolean)(implicit F: FunctorEmpty[F]): F[A] = F.filter(fa)(f)
}

final class FlattenOptionOps[F[_], A](val fa: F[Option[A]]) extends AnyVal {
  def flattenOption(fa: F[Option[A]])(implicit F: FunctorEmpty[F]): F[A] = F.flattenOption(fa)
}

object empty extends EmptySyntax
