package cats
package mtl

import cats.mtl.evidence._
import syntax.cartesian._
import syntax.applicative._

trait TellN[N <: Nat, F[_], L] {
  val monad: Monad[F]
  def tell(l: L): F[Unit]
  def writer[A](a: A, l: L)(implicit F: Applicative[F]): F[A] =
    a.pure[F] <* tell(l)
}
