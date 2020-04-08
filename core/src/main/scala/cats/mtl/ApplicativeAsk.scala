package cats
package mtl

import cats.data.{Kleisli, ReaderWriterStateT => RWST}

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
trait ApplicativeAsk[F[_], E] extends Serializable {
  def applicative: Applicative[F]

  def ask: F[E]

  def reader[A](f: E => A): F[A] = applicative.map(ask)(f)
}

private[mtl] trait LowPriorityApplicativeAskInstances {

  implicit def applicativeAskForMonadPartialOrder[F[_], G[_], E](
      implicit lift: MonadPartialOrder[F, G],
      F: ApplicativeAsk[F, E])
      : ApplicativeAsk[G, E] =
    new ApplicativeAsk[G, E] {
      val applicative = lift.monadG
      def ask = lift(F.ask)
    }
}

private[mtl] trait ApplicativeAskInstances extends LowPriorityApplicativeAskInstances {

  implicit def applicativeAskForKleisli[F[_]: Applicative, E]: ApplicativeAsk[Kleisli[F, E, *], E] =
    new ApplicativeAsk[Kleisli[F, E, *], E] {
      val applicative = Applicative[Kleisli[F, E, *]]

      def ask = Kleisli.ask[F, E]
    }

  implicit def applicativeAskForRWST[F[_]: Monad, E, L: Monoid, S]: ApplicativeAsk[RWST[F, E, L, S, *], E] =
    new ApplicativeAsk[RWST[F, E, L, S, *], E] {
      val applicative = Applicative[RWST[F, E, L, S, *]]

      def ask = RWST.ask[F, E, L, S]
    }
}

object ApplicativeAsk extends ApplicativeAskInstances {

  def apply[F[_], E](implicit applicativeAsk: ApplicativeAsk[F, E]): ApplicativeAsk[F, E] = applicativeAsk

  def const[F[_]: Applicative, E](e: E): ApplicativeAsk[F, E] = new ApplicativeAsk[F, E] {
    val applicative: Applicative[F] = Applicative[F]
    val ask: F[E] = applicative.pure(e)
  }

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
