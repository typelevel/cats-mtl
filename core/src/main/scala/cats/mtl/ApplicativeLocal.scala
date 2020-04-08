package cats
package mtl

import cats.data.{Kleisli, ReaderWriterStateT => RWST}

/**
  * `ApplicativeLocal[F, E]` lets you alter the `E` value that is observed by an `F[A]` value
  * using `ask`; the modification can only be observed from within that `F[A]` value.
  *
  * `ApplicativeLocal[F, E]` has three external laws:
  * {{{
  * def askReflectsLocal(f: E => E) = {
  *   local(f)(ask) <-> ask map f
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
trait ApplicativeLocal[F[_], E] extends ApplicativeAsk[F, E] with Serializable {
  def local[A](f: E => E)(fa: F[A]): F[A]

  def scope[A](e: E)(fa: F[A]): F[A] = local(_ => e)(fa)
}

private[mtl] trait LowPriorityApplicativeLocalInstances {

}

private[mtl] trait ApplicativeLocalInstances extends LowPriorityApplicativeLocalInstances {

  implicit def baseApplicativeLocalForKleisli[F[_]: Applicative, E]: ApplicativeLocal[Kleisli[F, E, *], E] =
    new ApplicativeLocal[Kleisli[F, E, *], E] {
      val applicative = Applicative[Kleisli[F, E, *]]
      def ask = Kleisli.ask[F, E]
      def local[A](f: E => E)(fa: Kleisli[F, E, A]) = fa.local(f)
    }

  implicit def baseApplicativeLocalForRWST[F[_]: Monad, E, L: Monoid, S]: ApplicativeLocal[RWST[F, E, L, S, *], E] =
    new ApplicativeLocal[RWST[F, E, L, S, *], E] {
      val applicative = Applicative[RWST[F, E, L, S, *]]
      def ask = RWST.ask[F, E, L, S]
      def local[A](f: E => E)(fa: RWST[F, E, L, S, A]) = fa.local(f)
    }
}

object ApplicativeLocal extends ApplicativeLocalInstances {
  def apply[F[_], A](implicit local: ApplicativeLocal[F, A]): ApplicativeLocal[F, A] = local

  def local[F[_], E, A](f: E => E)(fa: F[A])(implicit local: ApplicativeLocal[F, E]): F[A] = local.local(f)(fa)

  def scope[F[_], E, A](e: E)(fa: F[A])(implicit local: ApplicativeLocal[F, E]): F[A] = local.scope(e)(fa)
}
