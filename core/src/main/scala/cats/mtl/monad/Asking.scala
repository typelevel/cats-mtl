package cats
package mtl
package monad

/**
  * Asking has two external laws:
  * {{{
  * def askAddsNoEffects[A](fa: F[A]) = {
  *   (ask >> fa) == (fa << ask) == fa
  * }
  * def askIsNotAffectedByEffects[A](fa: F[A]) = {
  *   for {
  *     a1 <- ask
  *     a2 <- (fa >> ask)
  *   } yield (a1 == a2)
  * }
  * }}}
  *
  * Asking has one internal law:
  * {{{
  * def readerIsAskAndMap[A](f: E => A) = {
  *   ask.map(f) == reader(f)
  * }
  * }}}
  * Otherwise `Asking[F, E]` only denotes the availability of `E` values in the `F[_]` context.
  */
trait Asking[F[_], E] {
  val monad: Monad[F]

  def ask: F[E]

  def reader[A](f: E => A): F[A]
}

object Asking {

  def ask[F[_], E](implicit ask: Asking[F, E]): F[E] =
    ask.ask

  def askE[E] = new askEPartiallyApplied[E]

  def askF[F[_]] = new askFPartiallyApplied[F]

  @inline final private[mtl] class askEPartiallyApplied[E](val dummy: Boolean = false) extends AnyVal {
    @inline def apply[F[_]]()(implicit ask: Asking[F, E]): F[E] =
      ask.ask
  }

  @inline final private[mtl] class askFPartiallyApplied[F[_]](val dummy: Boolean = false) extends AnyVal {
    @inline def apply[E]()(implicit ask: Asking[F, E]): F[E] =
      ask.ask
  }

  @inline final private[mtl] class readerFEPartiallyApplied[F[_], E](val dummy: Boolean = false) extends AnyVal {
    @inline def apply[A](f: E => A)(implicit ask: Asking[F, E]): F[A] =
      ask.reader(f)
  }

  def readerFE[F[_], E] = new readerFEPartiallyApplied[F, E]

  def reader[F[_], E, A](fun: E => A)(implicit ask: Asking[F, E]): F[A] =
    ask.reader(fun)

}

