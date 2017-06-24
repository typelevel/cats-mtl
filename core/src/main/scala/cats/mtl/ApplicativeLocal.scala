package cats
package mtl

/**
  * `ApplicativeLocal` has one external law:
  * {{{
  * def askReflectsLocal(f: E => E) = {
  *   local(ask)(f) <-> ask map f
  * }
  * }}}
  *
  * `ApplicativeLocal` has one internal law:
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
  def local[F[_], E, A](fa: F[A])(f: E => E)(implicit local: ApplicativeLocal[F, E]): F[A] = local.local(fa)(f)

  def scope[F[_], E, A](fa: F[A])(e: E)(implicit local: ApplicativeLocal[F, E]): F[A] = local.scope(fa)(e)
}
