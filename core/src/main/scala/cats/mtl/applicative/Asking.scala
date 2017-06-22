package cats
package mtl
package applicative

/**
  * Asking has two external laws:
  * {{{
  * def askAddsNoEffects[A](fa: F[A]) = {
  *   (ask *> fa) <-> fa
  * }
  * def askIsNotAffected[A](fa: F[A]) = {
  *   (fa *> ask) <-> (ask <* fa)
  * } // ask "commutes" with all F[A].
  *   // this implies that Ask is purely for immutable environments,
  *   // as in the Reader monad, and in particular not the State monad
  *   // or Writer monad.
  * }}}
  *
  * Asking has one internal law:
  * {{{
  * def readerIsAskAndMap[A](f: E => A) = {
  *   ask.map(f) <-> reader(f)
  * }
  * }}}
  * Otherwise `Asking[F, E]` only denotes the availability of `E` values in the `F[_]` context,
  * which cannot be changed by previous `F[_]` effects.
  */
trait Asking[F[_], E] {
  val applicative: Applicative[F]

  def ask: F[E]

  def reader[A](f: E => A): F[A]
}

object Asking {

  def ask[F[_], E](implicit ask: Asking[F, E]): F[E] = {
    ask.ask
  }

  def askE[E] = new askEPartiallyApplied[E]

  def askF[F[_]] = new askFPartiallyApplied[F]

  @inline final private[mtl] class askEPartiallyApplied[E](val dummy: Boolean = false) extends AnyVal {
    @inline def apply[F[_]]()(implicit ask: Asking[F, E]): F[E] = {
      ask.ask
    }
  }

  @inline final private[mtl] class askFPartiallyApplied[F[_]](val dummy: Boolean = false) extends AnyVal {
    @inline def apply[E]()(implicit ask: Asking[F, E]): F[E] = {
      ask.ask
    }
  }

  @inline final private[mtl] class readerFEPartiallyApplied[F[_], E](val dummy: Boolean = false) extends AnyVal {
    @inline def apply[A](f: E => A)(implicit ask: Asking[F, E]): F[A] = {
      ask.reader(f)
    }
  }

  def readerFE[F[_], E] = new readerFEPartiallyApplied[F, E]

  def reader[F[_], E, A](fun: E => A)(implicit ask: Asking[F, E]): F[A] = {
    ask.reader(fun)
  }

}

