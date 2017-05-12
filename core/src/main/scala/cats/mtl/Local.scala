package cats
package mtl

import evidence.Nat

trait Local[F[_], E] {

  type N <: Nat

  val ask: Ask.Aux[N, F, E]

  def local[A](fa: F[A])(f: E => E): F[A]

  def scope[A](e: E)(fa: F[A]): F[A] =
    local(fa)(_ => e)
}

object Local {

  type Aux[N0 <: Nat, F[_], E] = Local[F, E] { type N = N0 }

}
