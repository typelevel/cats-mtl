package cats
package mtl

import cats.data.EitherT

trait ApplicativeHandle[F[_], E] extends FunctorRaise[F, E] with Serializable {
  def applicative: Applicative[F]

  def handleWith[A](fa: F[A])(f: E => F[A]): F[A]

  def attempt[A](fa: F[A]): F[Either[E, A]] =
    handleWith(applicative.map(fa)(Right(_): Either[E, A]))(e => applicative.pure(Left(e)))

  def attemptT[A](fa: F[A]): EitherT[F, E, A] =
    EitherT(attempt(fa))

  def handle[A](fa: F[A])(f: E => A): F[A] =
    handleWith(fa)(e => applicative.pure(f(e)))
}

private[mtl] trait ApplicativeHandleInstances {

  implicit def applicativeHandleForMonadError[F[_], E](implicit F: MonadError[F, E]): ApplicativeHandle[F, E] =
    new ApplicativeHandle[F, E] {
      def applicative = F
      def functor = F

      def raise[A](e: E) = F.raiseError(e)
      def handleWith[A](fa: F[A])(f: E => F[A]) = F.handleErrorWith(fa)(f)
    }
}

object ApplicativeHandle extends ApplicativeHandleInstances {
  def apply[F[_], E](implicit ev: ApplicativeHandle[F, E]): ApplicativeHandle[F, E] = ev
}
