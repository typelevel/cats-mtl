package cats
package mtl
package laws

import cats.laws.IsEq
import cats.laws.IsEqArrow
import cats.syntax.flatMap._
import cats.syntax.apply._

trait MonadStateLaws[F[_], S] {
  implicit val stateInstance: MonadState[F, S]
  implicit val monad: Monad[F] = stateInstance.monad

  import stateInstance._
  import monad.pure

  // external laws:
  def getThenSetDoesNothing: IsEq[F[Unit]] = {
    (get >>= set) <-> pure(())
  }

  def setThenGetReturnsSet(s: S): IsEq[F[S]] = {
    (set(s) *> get) <-> (set(s) *> pure(s))
  }

  def setThenSetSetsLast(s1: S, s2: S): IsEq[F[Unit]] = {
    set(s1) *> set(s2) <-> set(s2)
  }

  def getThenGetGetsOnce: IsEq[F[S]] = {
    get *> get <-> get
  }

  // internal laws:
  def setIsStateUnit(s: S): IsEq[F[Unit]] = {
    set(s) <-> state(_ => (s, ()))
  }

  def inpectIsState[A](f: S => A): IsEq[F[A]] = {
    inspect(f) <-> state(s => (s, f(s)))
  }

  def modifyIsState(f: S => S): IsEq[F[Unit]] = {
    modify(f) <-> state(s => (f(s), ()))
  }
}

object MonadStateLaws {
  def apply[F[_], S](implicit instance0: MonadState[F, S]): MonadStateLaws[F, S] = new MonadStateLaws[F, S] {
    override lazy val stateInstance: MonadState[F, S] = instance0
  }
}
