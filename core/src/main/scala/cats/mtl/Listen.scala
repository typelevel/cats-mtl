package cats
package mtl

import cats.mtl.evidence.Nat
import cats.syntax.flatMap._

trait Listen[F[_], L] {

  val tell: Tell[N, F, L]

  def listen[A](fa: F[A]): F[(A, L)]

  def pass[A](fa: F[(A, L => L)])(implicit F: Monad[F]): F[A] =
    listen(fa)
      .flatMap { case ((a, f), l) => tell.writer(a, f(l)) }

  type N <: Nat

}

object Listen {
  type Aux[N0 <: Nat, F[_], L] = Listen[F, L] {type N = N0}
}
