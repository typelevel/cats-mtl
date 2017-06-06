package cats
package mtl
package monad

import scala.util.control.NonFatal

/**
  * Raising has no laws not guaranteed by parametricity.
  *
  * Raising has one free law, i.e. a law guaranteed by parametricity:
  * {{{
  * def failThenFlatMapFails[A, B](ex: E, f: A => F[B]) = {
  *   fail(ex).flatMap(f) == fail(ex)
  * }
  * guaranteed by:
  *   fail[X](ex) <-> fail[F[Y]](ex) // parametricity
  *   fail[X](ex).map(f) <-> fail[F[Y]](ex)  // map must have no effect, because there's no value
  *   fail[X](ex).map(f).join <-> fail[F[Y]].join // add join to both sides
  *   fail(ex).flatMap(f) <-> fail(ex) // join is equal, because there's no inner value to flatten effects from
  *   // QED.
  * }}}
  */
trait Raising[F[_], E] {
  val monad: Monad[F]

  def raise[A](e: E): F[A]

  def catchNonFatal[A](a: => A)(f: Throwable => E): F[A] = {
    try {
      monad.pure(a)
    } catch {
      case NonFatal(ex) => raise(f(ex))
    }
  }
}

object Raising {
  def raise[F[_], E, A](e: E)(implicit raising: Raising[F, E]): F[A] =
    raising.raise(e)

  def raiseF[F[_]] =
    new raiseFPartiallyApplied[F]()

  def raiseE[E] =
    new raiseEPartiallyApplied[E]()

  final private[mtl] class raiseFPartiallyApplied[F[_]](val dummy: Boolean = false) extends AnyVal {
    @inline def apply[E, A](e: E)(implicit raise: Raising[F, E]): F[A] =
      raise.raise(e)
  }

  final private[mtl] class raiseEPartiallyApplied[E](val dummy: Boolean = false) extends AnyVal {
    @inline def apply[F[_], A](e: E)(implicit raise: Raising[F, E]): F[A] =
      raise.raise(e)
  }

}
