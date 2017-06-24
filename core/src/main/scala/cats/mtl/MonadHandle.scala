package cats
package mtl

import cats.data.EitherT

/**
  * `MonadHandle` has one external law:
  * {{{
  * def materializeRecoversRaise(e: E) = {
  *   attempt(raise(e)) <-> pure(Left(e))
  * }
  * }}}
  *
  * `MonadHandle` has one internal law:
  * {{{
  * def recoverWithIsMaterializeAndFlatMap[A](fa: F[A])(f: PartialFunction[E, A]): F[A] = {
  *   attempt(fa).flatMap {
  *     case Right(r) => pure(r)
  *     case Left(e) => f.lift(e).getOrElse(raise(e))
  *   }
  * }
  * }}}
  */
trait MonadHandle[F[_], E] {
  val monad: Monad[F]

  val raise: FunctorRaise[F, E]

  def attempt[A](fa: F[A]): F[E Either A]

  final def attemptT[A](fa: F[A]): EitherT[F, E, A] = EitherT(attempt(fa))

  def recover[A](fa: F[A])(f: PartialFunction[E, A]): F[A]

  def recoverWith[A](fa: F[A])(f: PartialFunction[E, F[A]]): F[A]
}