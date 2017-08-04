package cats
package mtl

/**
  * `MonadState[F, S]` is the capability to access and modify a state value
  * from inside the `F[_]` context, using `set(s: S): F[Unit]` and `get: F[S]`.
  *
  * MonadState has four external laws:
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
  * `MonadState` has two internal law:
  * {{{
  * def modifyIsGetThenSet(f: S => S) = {
  *   modify(f) <-> (inspect(f) flatMap set)
  * }
  *
  * def inspectLaw[A](f: S => A) = {
  *   inspect(f) <-> (get map f)
  * }
  * }}}
  *
  */
trait MonadState[F[_], S] extends Serializable {
  val monad: Monad[F]

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

  def inspect[F[_], S, A](f: S => A)(implicit state: MonadState[F, S]): F[A] =
    state.inspect(f)

  def apply[F[_], S](implicit monadState: MonadState[F, S]): MonadState[F, S] = monadState
}
