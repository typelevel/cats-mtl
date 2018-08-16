package cats
package mtl
package syntax

trait HandleSyntax {
  implicit def toHandleOps[F[_], A](fa: F[A]): HandleOps[F, A] = new HandleOps(fa)
}

final class HandleOps[F[_], A](val fa: F[A]) extends AnyVal {
  def handle[E](f: E => A)(implicit applicativeHandle: ApplicativeHandle[F, E]): F[A] =
    applicativeHandle.handle(fa)(f)
  def handleWith[E](f: E => F[A])(implicit applicativeHandle: ApplicativeHandle[F, E]): F[A] =
    applicativeHandle.handleWith(fa)(f)
}

object handle extends HandleSyntax
