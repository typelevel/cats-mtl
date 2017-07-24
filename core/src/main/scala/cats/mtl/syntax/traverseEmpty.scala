package cats
package mtl
package syntax

trait TraverseEmptySyntax {
  implicit def toTraverseEmptyOps[F[_], A](fa: F[A]): TraverseEmptyOps[F, A] = new TraverseEmptyOps[F, A](fa)
}

final class TraverseEmptyOps[F[_], A](val fa: F[A]) extends AnyVal {
  def traverseFilter[G[_]: Applicative, B](f: A => G[Option[B]])(implicit F: TraverseEmpty[F]): G[F[B]] = F.traverseFilter(fa)(f)
}

object traverseEmpty extends TraverseEmptySyntax
