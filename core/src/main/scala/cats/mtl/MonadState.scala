package cats
package mtl

/**
  * `MonadState[F, S]` is the capability to access and modify a state value
  * from inside the `F[_]` context, using `set(s: S): F[Unit]` and `get: F[S]`.
  *
  * MonadState has four external laws:
  * {{{
  *
  * def getThenSetDoesNothing = {
  *   get >>= set <-> pure(())
  * }
  * def setThenGetReturnsSet(s: S) = {
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
  * `MonadState` has three internal laws:
  * {{{
  * def setIsStateUnit(s: S) = {
  *  set(s) <-> state(_ => (s, ()))
  * }

  * def inpectIsState[A](f: S => A) = {
  *   inspect(f) <-> state(s => (s, f(s)))
  * }
  *
  * def modifyIsState(f: S => S) = {
  *   modify(f) <-> state(s => (f(s), ()))
  * }
  * }}}
  *
  */
trait MonadState[F[_], S] extends Serializable {
  val monad: Monad[F]

  def state[A](f: S => (S, A)): F[A]

  def get: F[S]

  def set(s: S): F[Unit]

  def inspect[A](f: S => A): F[A]

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

  def state[F[_], S, A](f: S => (S, A))(implicit state: MonadState[F, S]): F[A] =
    state.state(f)

  def inspect[F[_], S, A](f: S => A)(implicit state: MonadState[F, S]): F[A] =
    state.inspect(f)

  def apply[F[_], S](implicit monadState: MonadState[F, S]): MonadState[F, S] = monadState
}

trait DefaultMonadState[F[_], S] extends MonadState[F, S] {
  def get: F[S] = state(s => (s, s))
  def set(s: S): F[Unit] = state(_ => (s, ()))
  def inspect[A](f: S => A): F[A] = state(s => (s, f(s)))
  def modify(f: S => S): F[Unit] = state(s => (f(s), ()))
}
