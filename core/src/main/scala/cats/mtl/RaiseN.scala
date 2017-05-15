package cats
package mtl

import cats.data.EitherT
import evidence._

trait Raise[F[_], E] {
  val monad: Monad[F]

  type N <: Nat

  def raiseError[A](e: E): F[A]
}

object Raise {
  type Aux[N0 <: Nat, F[_], E] = Raise[F, E] {type N = N0}
}
