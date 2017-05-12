package cats
package mtl
package evidence

sealed trait Nat

object Nat {
  final class Succ[S <: Nat] extends Nat

  final class Zero extends Nat

  type _0 = Zero
  type _1 = Succ[_0]
  type _2 = Succ[_1]
  type _3 = Succ[_2]
  type _4 = Succ[_3]
  type _5 = Succ[_4]
  type _6 = Succ[_5]
}
