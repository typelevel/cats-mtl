package cats
package mtl

import scala.util.control.NonFatal

/**
  * `FunctorRaise` has no external laws.
  *
  * `FunctorRaise` has two internal laws:
  * {{{
  * def catchNonFatalDefault[A](a: => A)(f: Throwable => E)(implicit A: Applicative[F]) = {
  *   catchNonFatal(a)(f) <-> try {
  *     A.pure(a)
  *   } catch {
  *     case NonFatal(ex) => raise(f(ex))
  *   }
  * }
  *
  * def ensureDefault[A](fa: F[A])(error: => E)(predicate: A => Boolean)(implicit A: Monad[F]) = {
  *   ensure(fa)(error)(predicate) <-> for {
  *     a <- fa
  *     _ <- if (predicate(a)) pure(()) else raise(error)
  *   } yield a
  * }
  * }}}
  *
  * `FunctorRaise` has one free law, i.e. a law guaranteed by parametricity:
  * {{{
  * def failThenFlatMapFails[A, B](ex: E, f: A => F[B]) = {
  *   fail(ex).flatMap(f) <-> fail(ex)
  * }
  * guaranteed by:
  *   fail[X](ex) <-> fail[F[Y]](ex) // parametricity
  *   fail[X](ex).map(f) <-> fail[F[Y]](ex)  // map must have no effect, because there's no X value
  *   fail[X](ex).map(f).join <-> fail[F[Y]].join // add join to both sides
  *   fail(ex).flatMap(f) <-> fail(ex) // join is equal, because there's no inner value to flatten effects from
  *   // QED.
  * }}}
  */
trait FunctorRaise[F[_], E] extends Serializable {
  val functor: Functor[F]

  def raise[A](e: E): F[A]

  def catchNonFatal[A](a: => A)(f: Throwable => E)(implicit A: Applicative[F]): F[A] = {
    try {
      A.pure(a)
    } catch {
      case NonFatal(ex) => raise(f(ex))
    }
  }

  def ensure[A](fa: F[A])(error: => E)(predicate: A => Boolean)(implicit A: Monad[F]): F[A] =
    A.flatMap(fa)(a => if (predicate(a)) A.pure(a) else raise(error))
}

object FunctorRaise {
  def apply[F[_], E](implicit functorRaise: FunctorRaise[F, E]): FunctorRaise[F, E] = functorRaise

  def raise[F[_], E, A](e: E)(implicit raise: FunctorRaise[F, E]): F[A] =
    raise.raise(e)

  def raiseF[F[_]]: raiseFPartiallyApplied[F] = new raiseFPartiallyApplied[F]()

  def raiseE[E]: raiseEPartiallyApplied[E] = new raiseEPartiallyApplied[E]()

  final private[mtl] class raiseFPartiallyApplied[F[_]](val dummy: Boolean = false) extends AnyVal {
    @inline def apply[E, A](e: E)(implicit raise: FunctorRaise[F, E]): F[A] =
      raise.raise(e)
  }

  final private[mtl] class raiseEPartiallyApplied[E](val dummy: Boolean = false) extends AnyVal {
    @inline def apply[F[_], A](e: E)(implicit raise: FunctorRaise[F, E]): F[A] =
      raise.raise(e)
  }

}
