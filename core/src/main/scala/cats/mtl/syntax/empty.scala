package cats
package mtl
package syntax

trait EmptySyntax {
  implicit def toEmptyOps[F[_], A](fa: F[A]): EmptyOps[F, A] = new EmptyOps[F, A](fa)
}

final class EmptyOps[F[_], A](val fa: F[A]) extends AnyVal {
  def traverseFilter[G[_]: Applicative, B](f: A => G[Option[B]])(implicit F: TraverseEmpty[F]): G[F[B]] = F.traverseFilter(fa)(f)
}

object empty extends EmptySyntax
