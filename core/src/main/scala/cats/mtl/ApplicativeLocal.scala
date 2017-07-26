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
  * def localPureIsPure[A](a: A, f: E => E) = {
  *   local(f)(pure(a)) <-> pure(a)
  * }
  *
  * def localDistributesOverAp[A, B](fa: F[A], ff: F[A => B], f: E => E) = {
  *   local(f)(ff ap fa) <-> local(f)(ff) ap local(f)(fa)
  * }
  * }}}
  *
  * `ApplicativeLocal` has one internal law:
  * {{{
  * def scopeIsLocalConst(fa: F[A], e: E) = {
  *   scope(e)(fa) <-> local(_ => e)(fa)
  * }
  * }}}
  *
  */
trait ApplicativeLocal[F[_], E] extends Serializable {
  val ask: ApplicativeAsk[F, E]

  def local[A](f: E => E)(fa: F[A]): F[A]

  def scope[A](e: E)(fa: F[A]): F[A]
}

object ApplicativeLocal {
  def apply[F[_], A](implicit local: ApplicativeLocal[F, A]): ApplicativeLocal[F, A] = local

  def local[F[_], E, A](f: E => E)(fa: F[A])(implicit local: ApplicativeLocal[F, E]): F[A] = local.local(f)(fa)

  def scope[F[_], E, A](e: E)(fa: F[A])(implicit local: ApplicativeLocal[F, E]): F[A] = local.scope(e)(fa)
}
