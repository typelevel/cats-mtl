package cats
package mtl

/**
  * `MonadState` has four external laws:
  * {{{
  * def getThenSetDoesNothing = {
  *   get >>= set <-> pure(())
  * }
  * def setThenGetReturnsSetted(s: S) = {
  *   set(s) *> get <-> set(s) *> pure(s)
  * }
  * def setThenSetSetsLast(s1: S, s2: S) = {
  *   set(s1) *> set(s2) <-> set(s2)
  * }
  * def getThenGetGetsOnce = {
  *   get *> get <-> get
  * }
  * }}}
  *
  * `MonadState` has one internal law:
  * {{{
  * def modifyIsGetThenSet(f: S => S) = {
  *   modify(f) <-> (get map f) flatMap set
  * }
  * }}}
  *
  * Note that if you have a MonadState instance,
  * it cannot touch the same values as an ApplicativeAsk instance
  * because the laws of ApplicativeAsk prohibit the value being changed by effects.
  *
  */
trait MonadState[F[_], S] extends Serializable {
  val fMonad: Monad[F]

  def get: F[S]

  def set(s: S): F[Unit]

  def modify(f: S => S): F[Unit]
}


object MonadState {
  def get[F[_], S](implicit ev: MonadState[F, S]): F[S] =
    ev.get

  def set[F[_], S](newState: S)(implicit ev: MonadState[F, S]): F[Unit] =
    ev.set(newState)

  def setF[F[_]]: setFPartiallyApplied[F] = new setFPartiallyApplied[F]

  final private[mtl] class setFPartiallyApplied[F[_]](val dummy: Boolean = false) extends AnyVal {
    @inline def apply[E, A](e: E)(implicit state: MonadState[F, E]): F[Unit] =
      state.set(e)
  }

  def modify[F[_], S](f: S => S)(implicit state: MonadState[F, S]): F[Unit] =
    state.modify(f)
}
