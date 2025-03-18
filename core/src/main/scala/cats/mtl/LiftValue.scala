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

import cats.arrow.FunctionK
import cats.data.{ReaderWriterStateT => RWST, _}

import scala.annotation.implicitNotFound
import scala.{unchecked => uc}

/** Lifts values from the higher-kinded type `From` to the higher-kinded type `To`. */
@implicitNotFound("no way defined to lift values from ${From} to ${To}")
trait LiftValue[From[_], To[_]] extends Serializable {

  /**
   * Lifts `From[A]` into `To[A]`.
   *
   * @note
   *   This method is usually best implemented by a `liftF` method on `To`'s companion object.
   */
  def liftF[A](value: From[A]): To[A]

  /**
   * @return
   *   a higher-kinded function that lifts `From` into `To`.
   *
   * @note
   *   This method is usually best implemented by a `liftK` method on `To`'s companion object.
   */
  def liftK: From ~> To =
    new (From ~> To) {
      def apply[A](fa: From[A]): To[A] = liftF(fa)
    }

  /** @return an instance that lifts `From` to `To` and then `To` to `Last` */
  def andThen[Last[_]](that: LiftValue[To, Last]): LiftValue[From, Last] =
    LiftValue.Composed(this, that)

  /** @return an instance that lifts `First` to `From` and then `From` to `To` */
  def compose[First[_]](that: LiftValue[First, From]): LiftValue[First, To] =
    LiftValue.Composed(that, this)
}

object LiftValue {

  private[mtl] class Identity[F[_]] extends LiftValue[F, F] {
    def liftF[A](value: F[A]): F[A] = value
    override val liftK: F ~> F = FunctionK.id
  }

  private[this] val _identity = new Identity[({ type L[_] = Any })#L]

  private[this] final class Composed[From[_], Middle[_], To[_]] private (
      inner: LiftValue[From, Middle],
      outer: LiftValue[Middle, To]
  ) extends LiftValue[From, To] {
    def liftF[A](value: From[A]): To[A] = outer.liftF(inner.liftF(value))
  }

  private object Composed {
    def apply[From[_], Middle[_], To[_]](
        inner: LiftValue[From, Middle],
        outer: LiftValue[Middle, To]
    ): LiftValue[From, To] =
      if (inner.isInstanceOf[Identity[From @uc]]) outer.asInstanceOf[LiftValue[From, To]]
      else if (outer.isInstanceOf[Identity[Middle @uc]]) inner.asInstanceOf[LiftValue[From, To]]
      else new Composed(inner, outer)
  }

  def apply[From[_], To[_]](implicit lv: LiftValue[From, To]): LiftValue[From, To] = lv

  /**
   * @return
   *   an instance that uses the given `liftK` function to lift values of the higher-kinded type
   *   `From` into the higher-kinded type `To`
   */
  def fromLiftK[From[_], To[_]](lift: From ~> To): LiftValue[From, To] =
    new LiftValue[From, To] {
      override val liftK: From ~> To = lift
      def liftF[A](value: From[A]): To[A] = liftK(value)
    }

  implicit def id[F[_]]: LiftValue[F, F] = _identity.asInstanceOf[Identity[F]]

  implicit def eitherT[From[_], To[_]: Functor, L](
      implicit inner: LiftValue[From, To]
  ): LiftValue[From, EitherT[To, L, *]] =
    inner.andThen {
      new LiftValue[To, EitherT[To, L, *]] {
        def liftF[A](value: To[A]): EitherT[To, L, A] = EitherT.liftF(value)
      }
    }

  implicit def iorT[From[_], To[_]: Functor, L](
      implicit inner: LiftValue[From, To]
  ): LiftValue[From, IorT[To, L, *]] =
    inner.andThen {
      new LiftValue[To, IorT[To, L, *]] {
        def liftF[A](value: To[A]): IorT[To, L, A] = IorT.right(value)
      }
    }

  implicit def kleisli[From[_], To[_], A](
      implicit inner: LiftValue[From, To]
  ): LiftValue[From, Kleisli[To, A, *]] =
    inner.andThen {
      new LiftValue[To, Kleisli[To, A, *]] {
        def liftF[B](value: To[B]): Kleisli[To, A, B] = Kleisli.liftF(value)
      }
    }

  implicit def optionT[From[_], To[_]: Functor](
      implicit inner: LiftValue[From, To]
  ): LiftValue[From, OptionT[To, *]] =
    inner.andThen {
      new LiftValue[To, OptionT[To, *]] {
        def liftF[A](value: To[A]): OptionT[To, A] = OptionT.liftF(value)
      }
    }

  implicit def rwst[From[_], To[_]: Applicative, E, L: Monoid, S](
      implicit inner: LiftValue[From, To]
  ): LiftValue[From, RWST[To, E, L, S, *]] =
    inner.andThen {
      new LiftValue[To, RWST[To, E, L, S, *]] {
        def liftF[A](value: To[A]): RWST[To, E, L, S, A] = RWST.liftF(value)
      }
    }

  implicit def stateT[From[_], To[_]: Applicative, S](
      implicit inner: LiftValue[From, To]
  ): LiftValue[From, StateT[To, S, *]] =
    inner.andThen {
      new LiftValue[To, StateT[To, S, *]] {
        def liftF[A](value: To[A]): StateT[To, S, A] = StateT.liftF(value)
      }
    }

  implicit def writerT[From[_], To[_]: Applicative, L: Monoid](
      implicit inner: LiftValue[From, To]
  ): LiftValue[From, WriterT[To, L, *]] =
    inner.andThen {
      new LiftValue[To, WriterT[To, L, *]] {
        def liftF[A](value: To[A]): WriterT[To, L, A] = WriterT.liftF(value)
      }
    }
}
