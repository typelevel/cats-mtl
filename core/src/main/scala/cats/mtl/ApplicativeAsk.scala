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

import cats.data.{Kleisli, ReaderWriterStateT => RWST}

import scala.annotation.implicitNotFound

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
@implicitNotFound(
  "Could not find an implicit instance of ApplicativeAsk[${F}, ${E}]. If you have a\nvalue of type ${E} in scope, or a way of computing one, you may want to construct\na value of type Kleisli for this call-site, rather than type ${F}. An example type:\n\n  Kleisli[${F}, ${E}, *]\n\nIf you do not have an ${E} or a way of getting one, you should add\nan implicit parameter of this type to your function. For example:\n\n  (implicit fask: ApplicativeAsk[${F}, ${E}}])\n")
trait ApplicativeAsk[F[_], E] extends Serializable {
  def applicative: Applicative[F]

  def ask: F[E]

  def reader[A](f: E => A): F[A] = applicative.map(ask)(f)
}

private[mtl] trait ApplicativeAskForMonadPartialOrder[F[_], G[_], E]
    extends ApplicativeAsk[G, E] {
  val lift: MonadPartialOrder[F, G]
  val F: ApplicativeAsk[F, E]

  override def applicative = lift.monadG
  override def ask = lift(F.ask)
}

private[mtl] trait LowPriorityApplicativeAskInstances
    extends LowPriorityApplicativeAskInstancesCompat {

  implicit def applicativeAskForMonadPartialOrder[F[_], G[_], E](
      implicit lift0: MonadPartialOrder[F, G],
      F0: ApplicativeAsk[F, E]): ApplicativeAsk[G, E] =
    new ApplicativeAskForMonadPartialOrder[F, G, E] {
      val lift: MonadPartialOrder[F, G] = lift0
      val F: ApplicativeAsk[F, E] = F0
    }
}

private[mtl] trait ApplicativeAskInstances extends LowPriorityApplicativeAskInstances {

  implicit def applicativeAskForKleisli[F[_], E](
      implicit F: Applicative[F]): ApplicativeAsk[Kleisli[F, E, *], E] =
    ApplicativeLocal.baseApplicativeLocalForKleisli[F, E]

  implicit def applicativeAskForRWST[F[_], E, L, S](
      implicit F: Monad[F],
      L: Monoid[L]): ApplicativeAsk[RWST[F, E, L, S, *], E] =
    ApplicativeLocal.baseApplicativeLocalForRWST[F, E, L, S]
}

object ApplicativeAsk extends ApplicativeAskInstances {

  def apply[F[_], E](implicit applicativeAsk: ApplicativeAsk[F, E]): ApplicativeAsk[F, E] =
    applicativeAsk

  def const[F[_]: Applicative, E](e: E): ApplicativeAsk[F, E] =
    new ApplicativeAsk[F, E] {
      val applicative: Applicative[F] = Applicative[F]
      val ask: F[E] = applicative.pure(e)
    }

  def ask[F[_], E](implicit ask: ApplicativeAsk[F, E]): F[E] =
    ask.ask

  def askF[F[_]]: askFPartiallyApplied[F] = new askFPartiallyApplied[F]

  @inline final private[mtl] class askFPartiallyApplied[F[_]](val dummy: Boolean = false)
      extends AnyVal {
    @inline def apply[E]()(implicit ask: `ApplicativeAsk`[F, E]): F[E] =
      ask.ask
  }

  @inline final private[mtl] class readerFEPartiallyApplied[F[_], E](val dummy: Boolean = false)
      extends AnyVal {
    @inline def apply[A](f: E => A)(implicit ask: ApplicativeAsk[F, E]): F[A] =
      ask.reader(f)
  }

  def readerFE[F[_], E]: readerFEPartiallyApplied[F, E] = new readerFEPartiallyApplied[F, E]

  def reader[F[_], E, A](fun: E => A)(implicit ask: ApplicativeAsk[F, E]): F[A] =
    ask.reader(fun)

}
