package cats
package mtl
package monad

import cats.data.EitherT

/**
  * Handling has one external law:
  * {{{
  * def materializeRecoversRaise(e: E) = {
  *   materialize(raise(e)) == pure(Left(e))
  * }
  * }}}
  *
  * And one internal law:
  * {{{
  * def recoverWithIsMaterializeAndFlatMap[A](fa: F[A])(f: PartialFunction[E, A]): F[A] = {
  *   materialize(fa).flatMap {
  *     case Right(r) => pure(r)
  *     case Left(e) => f.lift(e).getOrElse(raise(e))
  *   }
  * }
  * }}}
  */
trait Handling[F[_], E] {
  val raise: Raising[F, E]

  def attempt[A](fa: F[A]): F[E Either A]

  final def attemptT[A](fa: F[A]): EitherT[F, E, A] = EitherT(attempt(fa))

  def recover[A](fa: F[A])(f: PartialFunction[E, A]): F[A]

  def recoverWith[A](fa: F[A])(f: PartialFunction[E, F[A]]): F[A]
}