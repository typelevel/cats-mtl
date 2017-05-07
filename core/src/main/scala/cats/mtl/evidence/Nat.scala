package cats.mtl.evidence

sealed trait Nat

object Nat {
  sealed trait Succ[S <: Nat] extends Nat

  sealed trait Zero extends Nat
}

