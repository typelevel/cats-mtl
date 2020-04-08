package cats
package mtl

import cats.data.{ReaderWriterStateT => RWST, StateT}

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
  def monad: Monad[F]

  def inspect[A](f: S => A): F[A] = monad.map(get)(f)

  def modify(f: S => S): F[Unit] = monad.flatMap(inspect(f))(set)

  def get: F[S]

  def set(s: S): F[Unit]
}

private[mtl] trait LowPriorityMonadStateInstances {

  implicit def monadStateForPartialOrder[F[_], G[_], S](
      implicit liftF: MonadPartialOrder[F, G],    // NB don't make this the *second* parameter; it won't infer
      ms: MonadState[F, S])
      : MonadState[G, S] =
    new MonadState[G, S] {
      val monad = liftF.monadG
      def get: G[S] = liftF(ms.get)
      def set(s: S): G[Unit] = liftF(ms.set(s))
    }
}

private[mtl] trait MonadStateInstances extends LowPriorityMonadStateInstances {

  implicit def monadStateForStateT[F[_]: Monad, S]: MonadState[StateT[F, S, *], S] =
    new MonadState[StateT[F, S, *], S] {
      val monad = Monad[StateT[F, S, *]]

      def get = StateT.get[F, S]

      def set(s: S) = StateT.set[F, S](s)
    }

  implicit def monadStateForRWST[F[_]: Monad, E, L: Monoid, S]: MonadState[RWST[F, E, L, S, *], S] =
    new MonadState[RWST[F, E, L, S, *], S] {
      val monad = Monad[RWST[F, E, L, S, *]]

      def get = RWST.get[F, E, L, S]

      def set(s: S) = RWST.set[F, E, L, S](s)
    }
}

object MonadState extends MonadStateInstances {
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
