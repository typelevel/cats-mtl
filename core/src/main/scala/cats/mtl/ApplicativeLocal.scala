/*
 * Copyright 2020 Typelevel
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cats
package mtl

import cats.data.{ReaderWriterStateT => RWST, _}

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
  def local[A](fa: F[A])(f: E => E): F[A]

  def scope[A](fa: F[A])(e: E): F[A] = local(fa)(_ => e)
}

private[mtl] trait LowPriorityApplicativeLocalInstances
    extends LowPriorityApplicativeLocalInstancesCompat {
  implicit def applicativeLocalForKleisli[F[_]: Monad, E, R](
      implicit F0: ApplicativeLocal[F, E]): ApplicativeLocal[Kleisli[F, R, *], E] =
    new ApplicativeLocal[Kleisli[F, R, *], E]
      with ApplicativeAskForMonadPartialOrder[F, Kleisli[F, R, *], E] {
      def local[A](fa: Kleisli[F, R, A])(f: E => E): Kleisli[F, R, A] =
        Kleisli(r => F0.local(fa.run(r))(f))
      val F: ApplicativeAsk[F, E] = F0
      val lift: MonadPartialOrder[F, Kleisli[F, R, *]] =
        MonadPartialOrder.monadPartialOrderForKleisli[F, R]
    }
}

private[mtl] trait ApplicativeLocalInstances extends LowPriorityApplicativeLocalInstances {

  implicit def baseApplicativeLocalForKleisli[F[_], E](
      implicit F: Applicative[F]): ApplicativeLocal[Kleisli[F, E, *], E] =
    new ApplicativeLocal[Kleisli[F, E, *], E] {
      def local[A](fa: Kleisli[F, E, A])(f: E => E) = fa.local(f)
      val applicative = Applicative[Kleisli[F, E, *]]
      def ask = Kleisli.ask[F, E]
    }

  implicit def baseApplicativeLocalForRWST[F[_], E, L, S](
      implicit F: Monad[F],
      L: Monoid[L]): ApplicativeLocal[RWST[F, E, L, S, *], E] =
    new ApplicativeLocal[RWST[F, E, L, S, *], E] {
      def local[A](fa: RWST[F, E, L, S, A])(f: E => E) = fa.local(f)
      val applicative = Applicative[RWST[F, E, L, S, *]]
      def ask = RWST.ask[F, E, L, S]
    }

  implicit def applicativeLocalForWriterT[F[_]: Monad, E, L: Monoid](
      implicit F0: ApplicativeLocal[F, E]): ApplicativeLocal[WriterT[F, L, *], E] =
    new ApplicativeLocal[WriterT[F, L, *], E]
      with ApplicativeAskForMonadPartialOrder[F, WriterT[F, L, *], E] {
      def local[A](fa: WriterT[F, L, A])(f: E => E): WriterT[F, L, A] =
        WriterT(F0.local(fa.run)(f))
      val F: ApplicativeAsk[F, E] = F0
      val lift: MonadPartialOrder[F, WriterT[F, L, *]] =
        MonadPartialOrder.monadPartialOrderForWriterT[F, L]
    }

  implicit def applicativeLocalForRWST[F[_]: Monad, E, R, L: Monoid, S](
      implicit F0: ApplicativeLocal[F, E]): ApplicativeLocal[RWST[F, R, L, S, *], E] =
    new ApplicativeLocal[RWST[F, R, L, S, *], E]
      with ApplicativeAskForMonadPartialOrder[F, RWST[F, R, L, S, *], E] {
      def local[A](fa: RWST[F, R, L, S, A])(f: E => E): RWST[F, R, L, S, A] =
        RWST((r, s) => F0.local(fa.run(r, s))(f))
      val F: ApplicativeAsk[F, E] = F0
      val lift: MonadPartialOrder[F, RWST[F, R, L, S, *]] =
        MonadPartialOrder.monadPartialOrderForRWST[F, R, L, S]
    }

  implicit def applicativeLocalForStateT[F[_]: Monad, E, S](
      implicit F0: ApplicativeLocal[F, E]): ApplicativeLocal[StateT[F, S, *], E] =
    new ApplicativeLocal[StateT[F, S, *], E]
      with ApplicativeAskForMonadPartialOrder[F, StateT[F, S, *], E] {
      def local[A](fa: StateT[F, S, A])(f: E => E): StateT[F, S, A] =
        StateT(s => F0.local(fa.run(s))(f))
      val F: ApplicativeAsk[F, E] = F0
      val lift: MonadPartialOrder[F, StateT[F, S, *]] =
        MonadPartialOrder.monadPartialOrderForStateT[F, S]
    }

  implicit def applicativeLocalForEitherT[F[_]: Monad, E, E2](
      implicit F0: ApplicativeLocal[F, E]): ApplicativeLocal[EitherT[F, E2, *], E] =
    new ApplicativeLocal[EitherT[F, E2, *], E]
      with ApplicativeAskForMonadPartialOrder[F, EitherT[F, E2, *], E] {
      def local[A](fa: EitherT[F, E2, A])(f: E => E): EitherT[F, E2, A] =
        EitherT(F0.local(fa.value)(f))
      val F: ApplicativeAsk[F, E] = F0
      val lift: MonadPartialOrder[F, EitherT[F, E2, *]] =
        MonadPartialOrder.monadPartialOrderForEitherT[F, E2]
    }

  implicit def applicativeLocalForOptionT[F[_]: Monad, E](
      implicit F0: ApplicativeLocal[F, E]): ApplicativeLocal[OptionT[F, *], E] =
    new ApplicativeLocal[OptionT[F, *], E]
      with ApplicativeAskForMonadPartialOrder[F, OptionT[F, *], E] {
      def local[A](fa: OptionT[F, A])(f: E => E): OptionT[F, A] =
        OptionT(F0.local(fa.value)(f))
      val F: ApplicativeAsk[F, E] = F0
      val lift: MonadPartialOrder[F, OptionT[F, *]] =
        MonadPartialOrder.monadPartialOrderForOptionT[F]
    }

  implicit def applicativeLocalForIorT[F[_]: Monad, E, E2: Semigroup](
      implicit F0: ApplicativeLocal[F, E]): ApplicativeLocal[IorT[F, E2, *], E] =
    new ApplicativeLocal[IorT[F, E2, *], E]
      with ApplicativeAskForMonadPartialOrder[F, IorT[F, E2, *], E] {
      def local[A](fa: IorT[F, E2, A])(f: E => E): IorT[F, E2, A] =
        IorT(F0.local(fa.value)(f))
      val F: ApplicativeAsk[F, E] = F0
      val lift: MonadPartialOrder[F, IorT[F, E2, *]] =
        MonadPartialOrder.monadPartialOrderForIorT[F, E2]
    }
}

object ApplicativeLocal extends ApplicativeLocalInstances {
  def apply[F[_], A](implicit local: ApplicativeLocal[F, A]): ApplicativeLocal[F, A] = local

  def local[F[_], E, A](fa: F[A])(f: E => E)(implicit local: ApplicativeLocal[F, E]): F[A] =
    local.local(fa)(f)

  def scope[F[_], E, A](fa: F[A])(e: E)(implicit local: ApplicativeLocal[F, E]): F[A] =
    local.scope(fa)(e)
}
