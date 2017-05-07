package cats
package mtl

import simulacrum.typeclass

@typeclass trait Local[F[_], E] {
  val read: Ask[F, E]
  def local[A](fa: F[A])(f: E => E): F[A]
  def scope[A](e: E)(fa: F[A]): F[A] =
    local(fa)(_ => e)

}
