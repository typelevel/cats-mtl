package cats
package mtl

import simulacrum.typeclass

@typeclass trait Raise[F[_], E] {
  val monad: Monad[F]
  def raiseError[A](e: E): F[A]
}
