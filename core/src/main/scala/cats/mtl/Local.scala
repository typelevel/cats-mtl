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

import scala.annotation.implicitNotFound

/**
  * `Local[F, E]` lets you alter the `E` value that is observed by an `F[A]` value
  * using `ask`; the modification can only be observed from within that `F[A]` value.
  *
  * `Local[F, E]` has three external laws:
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
  * `Local` has one internal law:
  * {{{
  * def scopeIsLocalConst(fa: F[A], e: E) = {
  *   scope(e)(fa) <-> local(_ => e)(fa)
  * }
  * }}}
  *
  */
@implicitNotFound(
  "Could not find an implicit instance of Local[${F}, ${E}]. If you have a\nvalue of type ${E} in scope, or a way of computing one, you may want to construct\na value of type Kleisli for this call-site, rather than type ${F}. An example type:\n\n  Kleisli[${F}, ${E}, *]\n\nIf you do not have an ${E} or a way of getting one, you should add\nan implicit parameter of this type to your function. For example:\n\n  (implicit flocal: Local[${F}, ${E}}])\n")
trait Local[F[_], E] extends Ask[F, E] with Serializable {
  def local[A](fa: F[A])(f: E => E): F[A]

  def scope[A](fa: F[A])(e: E): F[A] = local(fa)(_ => e)
}

private[mtl] trait LowPriorityLocalInstances extends LowPriorityLocalInstancesCompat {
  implicit def localForKleisli[F[_]: Monad, E, R](
      implicit F0: Local[F, E]): Local[Kleisli[F, R, *], E] =
    new Local[Kleisli[F, R, *], E] with AskForMonadPartialOrder[F, Kleisli[F, R, *], E] {
      def local[A](fa: Kleisli[F, R, A])(f: E => E): Kleisli[F, R, A] =
        Kleisli(r => F0.local(fa.run(r))(f))
      val F: Ask[F, E] = F0
      val lift: MonadPartialOrder[F, Kleisli[F, R, *]] =
        MonadPartialOrder.monadPartialOrderForKleisli[F, R]
    }
}

private[mtl] trait LocalInstances extends LowPriorityLocalInstances {

  implicit def baseLocalForKleisli[F[_], E](
      implicit F: Applicative[F]): Local[Kleisli[F, E, *], E] =
    new Local[Kleisli[F, E, *], E] {
      def local[A](fa: Kleisli[F, E, A])(f: E => E) = fa.local(f)
      val applicative = Applicative[Kleisli[F, E, *]]
      def ask = Kleisli.ask[F, E]
    }

  implicit def baseLocalForRWST[F[_], E, L, S](
      implicit F: Monad[F],
      L: Monoid[L]): Local[RWST[F, E, L, S, *], E] =
    new Local[RWST[F, E, L, S, *], E] {
      def local[A](fa: RWST[F, E, L, S, A])(f: E => E) = fa.local(f)
      val applicative = Applicative[RWST[F, E, L, S, *]]
      def ask = RWST.ask[F, E, L, S]
    }

  implicit def localForWriterT[F[_]: Monad, E, L: Monoid](
      implicit F0: Local[F, E]): Local[WriterT[F, L, *], E] =
    new Local[WriterT[F, L, *], E] with AskForMonadPartialOrder[F, WriterT[F, L, *], E] {
      def local[A](fa: WriterT[F, L, A])(f: E => E): WriterT[F, L, A] =
        WriterT(F0.local(fa.run)(f))
      val F: Ask[F, E] = F0
      val lift: MonadPartialOrder[F, WriterT[F, L, *]] =
        MonadPartialOrder.monadPartialOrderForWriterT[F, L]
    }

  implicit def localForRWST[F[_]: Monad, E, R, L: Monoid, S](
      implicit F0: Local[F, E]): Local[RWST[F, R, L, S, *], E] =
    new Local[RWST[F, R, L, S, *], E] with AskForMonadPartialOrder[F, RWST[F, R, L, S, *], E] {
      def local[A](fa: RWST[F, R, L, S, A])(f: E => E): RWST[F, R, L, S, A] =
        RWST((r, s) => F0.local(fa.run(r, s))(f))
      val F: Ask[F, E] = F0
      val lift: MonadPartialOrder[F, RWST[F, R, L, S, *]] =
        MonadPartialOrder.monadPartialOrderForRWST[F, R, L, S]
    }

  implicit def localForStateT[F[_]: Monad, E, S](
      implicit F0: Local[F, E]): Local[StateT[F, S, *], E] =
    new Local[StateT[F, S, *], E] with AskForMonadPartialOrder[F, StateT[F, S, *], E] {
      def local[A](fa: StateT[F, S, A])(f: E => E): StateT[F, S, A] =
        StateT(s => F0.local(fa.run(s))(f))
      val F: Ask[F, E] = F0
      val lift: MonadPartialOrder[F, StateT[F, S, *]] =
        MonadPartialOrder.monadPartialOrderForStateT[F, S]
    }

  implicit def localForEitherT[F[_]: Monad, E, E2](
      implicit F0: Local[F, E]): Local[EitherT[F, E2, *], E] =
    new Local[EitherT[F, E2, *], E] with AskForMonadPartialOrder[F, EitherT[F, E2, *], E] {
      def local[A](fa: EitherT[F, E2, A])(f: E => E): EitherT[F, E2, A] =
        EitherT(F0.local(fa.value)(f))
      val F: Ask[F, E] = F0
      val lift: MonadPartialOrder[F, EitherT[F, E2, *]] =
        MonadPartialOrder.monadPartialOrderForEitherT[F, E2]
    }

  implicit def localForOptionT[F[_]: Monad, E](
      implicit F0: Local[F, E]): Local[OptionT[F, *], E] =
    new Local[OptionT[F, *], E] with AskForMonadPartialOrder[F, OptionT[F, *], E] {
      def local[A](fa: OptionT[F, A])(f: E => E): OptionT[F, A] =
        OptionT(F0.local(fa.value)(f))
      val F: Ask[F, E] = F0
      val lift: MonadPartialOrder[F, OptionT[F, *]] =
        MonadPartialOrder.monadPartialOrderForOptionT[F]
    }

  implicit def localForIorT[F[_]: Monad, E, E2: Semigroup](
      implicit F0: Local[F, E]): Local[IorT[F, E2, *], E] =
    new Local[IorT[F, E2, *], E] with AskForMonadPartialOrder[F, IorT[F, E2, *], E] {
      def local[A](fa: IorT[F, E2, A])(f: E => E): IorT[F, E2, A] =
        IorT(F0.local(fa.value)(f))
      val F: Ask[F, E] = F0
      val lift: MonadPartialOrder[F, IorT[F, E2, *]] =
        MonadPartialOrder.monadPartialOrderForIorT[F, E2]
    }
}

object Local extends LocalInstances {
  def apply[F[_], A](implicit local: Local[F, A]): Local[F, A] = local

  def local[F[_], E, A](fa: F[A])(f: E => E)(implicit local: Local[F, E]): F[A] =
    local.local(fa)(f)

  def scope[F[_], E, A](fa: F[A])(e: E)(implicit local: Local[F, E]): F[A] =
    local.scope(fa)(e)
}
