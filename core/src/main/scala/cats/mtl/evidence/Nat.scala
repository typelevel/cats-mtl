package cats
package mtl
package evidence

sealed trait Nat

object Nat {
  final class Succ[S <: Nat] extends Nat

  final class Zero extends Nat
}
