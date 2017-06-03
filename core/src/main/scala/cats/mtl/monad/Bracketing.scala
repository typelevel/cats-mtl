package cats
package mtl
package monad

/**
  * Bracketing has no laws.
  *
  * Informally: if `F[A]` contains `A` values, `bracket(action)(bind, cleanup)`,
  * must include the effects of `cleanup` on all of those `A` values *once* for each `A` value.
  *
  * This must be the case *even if* `bind` returns an `F[B]` with no `B` values inside.
  */
trait Bracketing[F[_]] {
  val monad: Monad[F]

  def bracket[A, B, C](action: F[A])(bind: A => F[B],
                                     cleanup: A => F[C]): F[B]
}
