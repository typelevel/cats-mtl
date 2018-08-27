package cats.mtl

import cats.Applicative

import acyclic.skipped

/**
  * `ApplicativeAsk[F, E]` lets you access an `E` value in the `F[_]` context.
  *
  * Intuitively, this means that an `E` value is required as an input to get "out" of the `F[_]` context.
  *
  * `ApplicativeAsk[F, E]` has one external law:
  * {{{
  * def askAddsNoEffects[A](fa: F[A]) = {
  *   (ask *> fa) <-> fa
  * }
  * }}}
  *
  * `ApplicativeAsk[F, E]` has one internal law:
  * {{{
  * def readerIsAskAndMap[A](f: E => A) = {
  *   ask.map(f) <-> reader(f)
  * }
  * }}}
  *
  */
trait ApplicativeAskClass[F[_], E] extends Serializable {
  val applicative: Applicative[F]

  def ask: F[E]

  def reader[A](f: E => A): F[A]
}

object ApplicativeAsk {

  def apply[F[_], E](implicit applicativeAsk: ApplicativeAsk[F, E]): ApplicativeAsk[F, E] = applicativeAsk

  def ask[F[_], E](implicit ask: ApplicativeAsk[F, E]): F[E] = {
    ask.ask
  }

  def askF[F[_]]: askFPartiallyApplied[F] = new askFPartiallyApplied[F]

  @inline final private[mtl] class askFPartiallyApplied[F[_]](val dummy: Boolean = false) extends AnyVal {
    @inline def apply[E]()(implicit ask: `ApplicativeAsk`[F, E]): F[E] = {
      ask.ask
    }
  }

  @inline final private[mtl] class readerFEPartiallyApplied[F[_], E](val dummy: Boolean = false) extends AnyVal {
    @inline def apply[A](f: E => A)(implicit ask: ApplicativeAsk[F, E]): F[A] = {
      ask.reader(f)
    }
  }

  def readerFE[F[_], E]: readerFEPartiallyApplied[F, E] = new readerFEPartiallyApplied[F, E]

  def reader[F[_], E, A](fun: E => A)(implicit ask: ApplicativeAsk[F, E]): F[A] = {
    ask.reader(fun)
  }

}

trait DefaultApplicativeAskClass[F[_], E] extends ApplicativeAskClass[F, E] {
  def reader[A](f: E => A): F[A] = applicative.map(ask)(f)
}
