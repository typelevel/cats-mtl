package cats
package mtl
package syntax

trait LocalSyntax {
  implicit def toLocalOps[F[_], A](fa: F[A]): LocalOps[F, A] = new LocalOps(fa)
}

final class LocalOps[F[_], A](val fa: F[A]) extends AnyVal {
  def local[E](f: E => E)(implicit applicativeLocal: ApplicativeLocal[F, E]): F[A] =
    applicativeLocal.local(f)(fa)
  def scope[E](e: E)(implicit applicativeLocal: ApplicativeLocal[F, E]): F[A] =
    applicativeLocal.scope(e)(fa)
}

object local extends LocalSyntax
