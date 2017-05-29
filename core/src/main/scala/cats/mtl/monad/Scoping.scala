package cats
package mtl
package monad

/**
  * Scoping has one external law:
  * {{{
  * def askReflectsLocal(f: E => E) = {
  *   local(ask)(f) == ask map f
  * }
  * }}}
  *
  * Scoping has one internal law:
  * {{{
  * def scopeIsLocalConst(fa: F[A], e: E) = {
  *   local(fa)(_ => e) == scope(fa)(e)
  * }
  * }}}
  *
  */
trait Scoping[F[_], E] {
  val ask: Asking[F, E]

  def local[A](fa: F[A])(f: E => E): F[A]

  def scope[A](fa: F[A])(e: E): F[A]
}

