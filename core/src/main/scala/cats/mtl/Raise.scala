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

import cats.data._

import scala.annotation.implicitNotFound
import scala.util.control.NonFatal

/**
  * `Raise[F, E]` expresses the ability to raise errors of type `E` in a functorial `F[_]` context.
  * This means that a value of type `F[A]` may contain no `A` values but instead an `E` error value,
  * and further `map` calls will not have any values to execute the passed function on.
  *
  * `Raise` has no external laws.
  *
  * `Raise` has two internal laws:
  * {{{
  * def catchNonFatalDefault[A](a: => A)(f: Throwable => E)(implicit A: Applicative[F]) = {
  *   catchNonFatal(a)(f) <-> try {
  *     A.pure(a)
  *   } catch {
  *     case NonFatal(ex) => raise(f(ex))
  *   }
  * }
  *
  * def ensureDefault[A](fa: F[A])(error: => E)(predicate: A => Boolean)(implicit A: Monad[F]) = {
  *   ensure(fa)(error)(predicate) <-> for {
  *     a <- fa
  *     _ <- if (predicate(a)) pure(()) else raise(error)
  *   } yield a
  * }
  * }}}
  *
  * `Raise` has one free law, i.e. a law guaranteed by parametricity:
  * {{{
  * def failThenFlatMapFails[A, B](ex: E, f: A => F[B]) = {
  *   fail(ex).flatMap(f) <-> fail(ex)
  * }
  * guaranteed by:
  *   fail[X](ex) <-> fail[F[Y]](ex) // parametricity
  *   fail[X](ex).map(f) <-> fail[F[Y]](ex)  // map must have no effect, because there's no X value
  *   fail[X](ex).map(f).join <-> fail[F[Y]].join // add join to both sides
  *   fail(ex).flatMap(f) <-> fail(ex) // join is equal, because there's no inner value to flatten effects from
  *   // QED.
  * }}}
  */
@implicitNotFound(
  "Could not find an implicit instance of Raise[${F}, ${E}]. If you have\na good way of handling errors of type ${E} at this location, you may want\nto construct a value of type EitherT for this call-site, rather than ${F}.\nAn example type:\n\n  EitherT[${F}, ${E}, *]\n\nThis is analogous to writing try/catch around this call. The EitherT will\n\"catch\" the errors of type ${E}.\n\nIf you do not wish to handle errors of type ${E} at this location, you should\nadd an implicit parameter of this type to your function. For example:\n\n  (implicit fraise: Raise[${F}, ${E}}])\n")
trait Raise[F[_], E] extends Serializable {
  def functor: Functor[F]

  def raise[A](e: E): F[A]

  def catchNonFatal[A](a: => A)(f: Throwable => E)(implicit A: Applicative[F]): F[A] = {
    try A.pure(a)
    catch {
      case NonFatal(ex) => raise(f(ex))
    }
  }

  def ensure[A](fa: F[A])(error: => E)(predicate: A => Boolean)(implicit A: Monad[F]): F[A] =
    A.flatMap(fa)(a => if (predicate(a)) A.pure(a) else raise(error))
}

private[mtl] trait RaiseMonadPartialOrder[F[_], G[_], E] extends Raise[G, E] {
  val lift: MonadPartialOrder[F, G]
  val F: Raise[F, E]

  override def functor = lift.monadG
  override def raise[A](e: E) = lift(F.raise(e))
}

private[mtl] trait LowPriorityRaiseInstances {
  implicit def raiseForMonadPartialOrder[F[_], G[_]: Functor, E](
      implicit F: Raise[F, E],
      lift: MonadPartialOrder[F, G]
  ): Raise[G, E] =
    new Raise[G, E] {
      val functor = Functor[G]
      def raise[A](e: E) = lift(F.raise(e))
    }
}

private[mtl] trait RaiseInstances extends LowPriorityRaiseInstances {
  implicit final def raiseEitherT[M[_], E](implicit M: Monad[M]): Raise[EitherTC[M, E]#l, E] =
    Handle.handleEitherT

  implicit final def raiseEither[E]: Raise[EitherC[E]#l, E] =
    Handle.handleEither

  implicit final def raiseOptionT[M[_]](implicit M: Monad[M]): Raise[OptionTC[M]#l, Unit] =
    Handle.handleOptionT

  implicit final def raiseOption[E]: Raise[Option, Unit] =
    Handle.handleOption

  implicit final def raiseValidated[E](implicit E: Semigroup[E]): Raise[Validated[E, ?], E] =
    Handle.handleValidated

  implicit final def raiseIor[E](implicit E: Semigroup[E]): Raise[Ior[E, *], E] =
    Handle.handleIor[E]

  implicit final def raiseIorT[F[_], E](
      implicit E: Semigroup[E],
      F: Monad[F]): Raise[IorT[F, E, *], E] =
    Handle.handleIorT[F, E]

}

object Raise extends RaiseInstances {
  def apply[F[_], E](implicit raise: Raise[F, E]): Raise[F, E] =
    raise

  def raise[F[_], E, A](e: E)(implicit raise: Raise[F, _ >: E]): F[A] =
    raise.raise(e)

  def raiseF[F[_]]: raiseFPartiallyApplied[F] = new raiseFPartiallyApplied[F]()

  final private[mtl] class raiseFPartiallyApplied[F[_]](val dummy: Boolean = false)
      extends AnyVal {
    @inline def apply[E, A](e: E)(implicit raise: Raise[F, _ >: E]): F[A] =
      raise.raise(e)
  }

}
