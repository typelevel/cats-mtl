package cats
package mtl
package laws

import cats.laws.IsEq
import cats.laws.IsEqArrow
import cats.syntax.functor._

trait FunctorRaiseLaws[F[_], E] {
  implicit val raiseInstance: FunctorRaise[F, E]
  implicit val functor: Functor[F] = raiseInstance.functor

  import raiseInstance._

  // free law:
  def failThenFlatMapFails[A](ex: E, f: A => A): IsEq[F[A]] =
    raise(ex).map(f) <-> raise(ex)

}

object FunctorRaiseLaws {
  def apply[F[_], E](implicit instance0: FunctorRaise[F, E]): FunctorRaiseLaws[F, E] = {
    new FunctorRaiseLaws[F, E] {
      override lazy val raiseInstance: FunctorRaise[F, E] = instance0
    }
  }
}
