package cats
package mtl

import cats.mtl.evidence.Nat

trait Handle[F[_], E] {
  val raise: RaiseN.Aux[N, F, E]

  type N <: Nat

  def materialize[A](fa: F[A]): F[E Either A]
}

object Handle {
  type Aux[N0 <: Nat, F[_], E] = Handle[F, E] {type N = N0}
}
