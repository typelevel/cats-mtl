/*
 * Copyright 2021 Typelevel
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

import cats.data.{Kleisli, ReaderWriterStateT => RWST}

import scala.annotation.implicitNotFound

/**
 * `Ask[F, E]` lets you access an `E` value in the `F[_]` context.
 *
 * Intuitively, this means that an `E` value is required as an input to get "out" of the `F[_]`
 * context.
 *
 * `Ask[F, E]` has one external law:
 * {{{
 * def askAddsNoEffects[A](fa: F[A]) = {
 *   (ask *> fa) <-> fa
 * }
 * }}}
 *
 * `Ask[F, E]` has one internal law:
 * {{{
 * def readerIsAskAndMap[A](f: E => A) = {
 *   ask.map(f) <-> reader(f)
 * }
 * }}}
 */
@implicitNotFound(
  "Could not find an implicit instance of Ask[${F}, ${E}]. If you have a\nvalue of type ${E} in scope, or a way of computing one, you may want to construct\na value of type Kleisli for this call-site, rather than type ${F}. An example type:\n\n  Kleisli[${F}, ${E}, *]\n\nIf you do not have an ${E} or a way of getting one, you should add\nan implicit parameter of this type to your function. For example:\n\n  (implicit fask: Ask[${F}, ${E}])\n")
trait Ask[F[_], +E] extends Serializable {
  def applicative: Applicative[F]

  def ask[E2 >: E]: F[E2]

  def reader[A](f: E => A): F[A] = applicative.map(ask)(f)
}

private[mtl] trait AskForMonadPartialOrder[F[_], G[_], E] extends Ask[G, E] {
  val lift: MonadPartialOrder[F, G]
  val F: Ask[F, E]

  override def applicative = lift.monadG
  override def ask[E2 >: E] = lift(F.ask)
}

private[mtl] trait LowPriorityAskInstances extends LowPriorityAskInstancesCompat {

  def askForMonadPartialOrder[F[_], G[_], E](
      implicit lift0: MonadPartialOrder[F, G],
      F0: Ask[F, E]): Ask[G, E] =
    new AskForMonadPartialOrder[F, G, E] {
      val lift: MonadPartialOrder[F, G] = lift0
      val F: Ask[F, E] = F0
    }
}

private[mtl] trait AskInstances extends LocalInstances with LowPriorityAskInstances {

  def askForKleisli[F[_], E](implicit F: Applicative[F]): Ask[Kleisli[F, E, *], E] =
    baseLocalForKleisli[F, E]

  def askForRWST[F[_], E, L, S](
      implicit F: Monad[F],
      L: Monoid[L]): Ask[RWST[F, E, L, S, *], E] =
    baseLocalForRWST[F, E, L, S]
}

object Ask extends AskInstances {

  def apply[F[_], E](implicit ask: Ask[F, E]): Ask[F, E] =
    ask

  def const[F[_]: Applicative, E](e: E): Ask[F, E] =
    new Ask[F, E] {
      val applicative: Applicative[F] = Applicative[F]
      def ask[E2 >: E]: F[E2] = applicative.pure(e)
    }

  def ask[F[_], E](implicit ask: Ask[F, E]): F[E] =
    ask.ask

  def askF[F[_]]: askFPartiallyApplied[F] = new askFPartiallyApplied[F]

  @inline final private[mtl] class askFPartiallyApplied[F[_]](val dummy: Boolean = false)
      extends AnyVal {
    @inline def apply[E]()(implicit ask: `Ask`[F, E]): F[E] =
      ask.ask
  }

  @inline final private[mtl] class readerFEPartiallyApplied[F[_], E](val dummy: Boolean = false)
      extends AnyVal {
    @inline def apply[A](f: E => A)(implicit ask: Ask[F, E]): F[A] =
      ask.reader(f)
  }

  def readerFE[F[_], E]: readerFEPartiallyApplied[F, E] = new readerFEPartiallyApplied[F, E]

  def reader[F[_], E, A](fun: E => A)(implicit ask: Ask[F, E]): F[A] =
    ask.reader(fun)

}
