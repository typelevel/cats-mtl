package cats
package mtl

import cats.syntax.flatMap._
import simulacrum.typeclass

@typeclass trait Listen[F[_], L] {
  val tell: Tell[F, L]
  def listen[A](fa: F[A]): F[(A, L)]
  def pass[A](fa: F[(A, L => L)])(implicit F: Monad[F]): F[A] =
    listen(fa)
      .flatMap { case ((a, f), l) => tell.writer(a, f(l)) }

}
