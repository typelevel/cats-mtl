package cats
package mtl
package monad

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
  * def handleErrorWithIsMaterializedMap[A](fa: F[A])(f: PartialFunction[E, A]): F[A] = {
  *   materialize(fa).flatMap {
  *     case Right(r) => pure(r)
  *     case Left(e) => f.lift(e).fold(raise(e))(pure(_))
  *   }
  * }
  * }}}
  */
trait Handling[F[_], E] {
  val raise: Raising[F, E]

  def materialize[A](fa: F[A]): F[E Either A]

  def handleErrorWith[A](fa: F[A])(f: PartialFunction[E, A]): F[A]
}