package cats
package mtl
package monad

/**
  * Stateful has four external laws:
  * {{{
  * def getThenSetDoesNothing = {
  *   get >> set == pure(())
  * }
  * def setThenGetReturnsSetted(s: S) = {
  *   set(s) >> get == set(s) >> pure(s)
  * }
  * def setThenSetSetsLast(s1: S, s2: S) = {
  *   set(s1) >> set(s2) == set(s2)
  * }
  * def getThenGetGetsOnce = {
  *   get >> get == get
  * }
  * }}}
  *
  * Stateful has one internal law:
  * {{{
  * def modifyIsGetThenSet(f: S => S) = {
  *   modify(f) == (get map f) flatMap set
  * }
  * }}}
  *
  */
trait Stateful[F[_], S] {
  val fMonad: Monad[F]

  val ask: Asking[F, S]

  def get: F[S]

  def set(s: S): F[Unit]

  def modify(f: S => S): F[Unit]
}


object Stateful {
  def get[F[_], S](implicit ev: Stateful[F, S]): F[S] =
    ev.get

  def set[F[_], S](newState: S)(implicit ev: Stateful[F, S]): F[Unit] =
    ev.set(newState)

  def setF[F[_]] = new setFPartiallyApplied[F]

  final private[mtl] class setFPartiallyApplied[F[_]](val dummy: Boolean = false) extends AnyVal {
    @inline def apply[E, A](e: E)(implicit stateful: Stateful[F, E]): F[Unit] =
      stateful.set(e)
  }

  def modify[F[_], S](f: S => S)(implicit ev: Stateful[F, S]): F[Unit] =
    ev.modify(f)
}
