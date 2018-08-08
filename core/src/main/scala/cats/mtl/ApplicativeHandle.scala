package cats
package mtl

trait ApplicativeHandle[F[_], E] {
  val raise: FunctorRaise[F, E]
  val applicative: Applicative[F]

  def handleWith[A](fa: F[A])(f: E => F[A]): F[A]

  def attempt[A](fa: F[A]): F[Either[E, A]]

  def handle[A](fa: F[A])(f: E => A): F[A]
}



object ApplicativeHandle {
  def apply[F[_], E](implicit ev: ApplicativeHandle[F, E]): ApplicativeHandle[F, E] = ev
}


trait DefaultApplicativeHandle[F[_], E] extends ApplicativeHandle[F, E] {
  def attempt[A](fa: F[A]): F[Either[E, A]] =
    handleWith(applicative.map(fa)(Right(_): Either[E, A]))(e => applicative.pure(Left(e)))

  def handle[A](fa: F[A])(f: E => A): F[A] =
    handleWith(fa)(e => applicative.pure(f(e)))
}
