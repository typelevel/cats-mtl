package cats
package mtl

/**
  * `ApplicativeLocal[F, E]` lets you alter the `E` value that is observed by an `F[A]` value
  * using `ask`; the modification can only be observed from within that `F[A]` value.
  *
  * `ApplicativeLocal[F, E]` has one external law:
  * {{{
  * def askReflectsLocal(f: E => E) = {
  *   local(ask)(f) <-> ask map f
  * }
  *
  * def localPureIsPure[A](a: A, f: R => R) = {
  *   local(f)(pure(a)) <-> pure(a)
  * }
  *
  * def localDistributesOverAp[A, B](fa: F[A], ff: F[A => B], f: R => R) = {
  *   local(f)(fa ap ff) <-> local(f)(fa) ap local(f)(ff)
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
trait ApplicativeLocal[F[_], E] extends Serializable {
  val ask: ApplicativeAsk[F, E]

  def local[A](fa: F[A])(f: E => E): F[A]

  def scope[A](fa: F[A])(e: E): F[A]
}

object ApplicativeLocal {
  def apply[F[_], A](implicit local: ApplicativeLocal[F, A]): ApplicativeLocal[F, A] = local

  def local[F[_], E, A](fa: F[A])(f: E => E)(implicit local: ApplicativeLocal[F, E]): F[A] = local.local(fa)(f)

  def scope[F[_], E, A](fa: F[A])(e: E)(implicit local: ApplicativeLocal[F, E]): F[A] = local.scope(fa)(e)
}
