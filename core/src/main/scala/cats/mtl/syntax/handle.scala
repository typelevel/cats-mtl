package cats
package mtl
package syntax

import cats.data.EitherT

trait HandleSyntax {
  implicit def toHandleOps[F[_], A](fa: F[A]): HandleOps[F, A] = new HandleOps(fa)
}

final class HandleOps[F[_], A](val fa: F[A]) extends AnyVal {
  def attempt[E](implicit applicativeHandle: ApplicativeHandle[F, E]): F[Either[E, A]] =
    applicativeHandle.attempt(fa)
  def attemptT[E](implicit applicativeHandle: ApplicativeHandle[F, E]): EitherT[F, E, A] =
    applicativeHandle.attemptT(fa)
  def handle[E](f: E => A)(implicit applicativeHandle: ApplicativeHandle[F, E]): F[A] =
    applicativeHandle.handle(fa)(f)
  def handleWith[E](f: E => F[A])(implicit applicativeHandle: ApplicativeHandle[F, E]): F[A] =
    applicativeHandle.handleWith(fa)(f)
}

object handle extends HandleSyntax
