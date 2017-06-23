package cats
package mtl

/**
  * Scoping has one external law:
  * {{{
  * def askReflectsLocal(f: E => E) = {
  *   local(ask)(f) <-> ask map f
  * }
  * }}}
  *
  * Scoping has one internal law:
  * {{{
  * def scopeIsLocalConst(fa: F[A], e: E) = {
  *   local(fa)(_ => e) <-> scope(fa)(e)
  * }
  * }}}
  *
  */
trait ApplicativeLocal[F[_], E] {
  val ask: ApplicativeAsk[F, E]

  def local[A](fa: F[A])(f: E => E): F[A]

  def scope[A](fa: F[A])(e: E): F[A]
}

object ApplicativeLocal {
  def local[F[_], E, A](fa: F[A])(f: E => E)(implicit scoping: ApplicativeLocal[F, E]): F[A] = scoping.local(fa)(f)

  def scope[F[_], E, A](fa: F[A])(e: E)(implicit scoping: ApplicativeLocal[F, E]): F[A] = scoping.scope(fa)(e)
}
