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

import cats.data._

import scala.annotation.implicitNotFound
import scala.{unchecked => uc}

/**
 * Lifts values and scope transformations from the higher-kinded type `From` to the
 * higher-kinded type `To`.
 */
@implicitNotFound("no way defined to lift values and scopes from ${From} to ${To}")
trait LiftKind[From[_], To[_]] extends LiftValue[From, To] with Serializable {

  /**
   * Modifies the context of `To[A]` using the given scope transformation in `From`.
   *
   * @note
   *   This method is usually best implemented by a `mapK` method on `To`.
   */
  def limitedMapK[A](value: To[A])(scope: From ~> From): To[A]

  /**
   * Lifts a scope transformation in `From` into a scope operation in `To`.
   *
   * @note
   *   Implementors SHOULD NOT override this method; the only reason it is not final is for
   *   optimization of the identity case.
   */
  def liftScope(scope: From ~> From): To ~> To =
    new (To ~> To) {
      def apply[A](fa: To[A]): To[A] = limitedMapK(fa)(scope)
    }

  /** @return an instance that lifts `From` to `To` and then `To` to `Last` */
  def andThen[Last[_]](that: LiftKind[To, Last]): LiftKind[From, Last] =
    LiftKind.Composed(this, that)

  /** @return an instance that lifts `First` to `From` and then `From` to `To` */
  def compose[First[_]](that: LiftKind[First, From]): LiftKind[First, To] =
    LiftKind.Composed(that, this)
}

object LiftKind {

  private[this] final class Identity[F[_]] extends LiftValue.Identity[F] with LiftKind[F, F] {
    def limitedMapK[A](value: F[A])(scope: F ~> F): F[A] = scope(value)
    override def liftScope(scope: F ~> F): F ~> F = scope
  }

  private[this] val _identity = new Identity[({ type L[_] = Any })#L]

  private[this] final class Composed[From[_], Middle[_], To[_]] private (
      inner: LiftKind[From, Middle],
      outer: LiftKind[Middle, To]
  ) extends LiftKind[From, To] {
    def liftF[A](value: From[A]): To[A] = outer.liftF(inner.liftF(value))
    def limitedMapK[A](value: To[A])(scope: From ~> From): To[A] =
      outer.limitedMapK(value)(inner.liftScope(scope))
    override def liftScope(scope: From ~> From): To ~> To =
      outer.liftScope(inner.liftScope(scope))
  }

  private object Composed {
    def apply[From[_], Middle[_], To[_]](
        inner: LiftKind[From, Middle],
        outer: LiftKind[Middle, To]
    ): LiftKind[From, To] =
      if (inner.isInstanceOf[Identity[From @uc]]) outer.asInstanceOf[LiftKind[From, To]]
      else if (outer.isInstanceOf[Identity[Middle @uc]]) inner.asInstanceOf[LiftKind[From, To]]
      else new Composed(inner, outer)
  }

  def apply[From[_], To[_]](implicit lk: LiftKind[From, To]): LiftKind[From, To] = lk

  implicit def id[F[_]]: LiftKind[F, F] = _identity.asInstanceOf[Identity[F]]

  implicit def eitherT[From[_], To[_]: Functor, L](
      implicit inner: LiftKind[From, To]
  ): LiftKind[From, EitherT[To, L, *]] =
    inner.andThen {
      new LiftKind[To, EitherT[To, L, *]] {
        def liftF[A](value: To[A]): EitherT[To, L, A] = EitherT.liftF(value)
        def limitedMapK[A](value: EitherT[To, L, A])(scope: To ~> To): EitherT[To, L, A] =
          value.mapK(scope)
      }
    }

  implicit def iorT[From[_], To[_]: Functor, L](
      implicit inner: LiftKind[From, To]
  ): LiftKind[From, IorT[To, L, *]] =
    inner.andThen {
      new LiftKind[To, IorT[To, L, *]] {
        def liftF[A](value: To[A]): IorT[To, L, A] = IorT.right(value)
        def limitedMapK[A](value: IorT[To, L, A])(scope: To ~> To): IorT[To, L, A] =
          value.mapK(scope)
      }
    }

  implicit def kleisli[From[_], To[_], A](
      implicit inner: LiftKind[From, To]
  ): LiftKind[From, Kleisli[To, A, *]] =
    inner.andThen {
      new LiftKind[To, Kleisli[To, A, *]] {
        def liftF[B](value: To[B]): Kleisli[To, A, B] = Kleisli.liftF(value)
        def limitedMapK[B](value: Kleisli[To, A, B])(scope: To ~> To): Kleisli[To, A, B] =
          value.mapK(scope)
      }
    }

  implicit def optionT[From[_], To[_]: Functor](
      implicit inner: LiftKind[From, To]
  ): LiftKind[From, OptionT[To, *]] =
    inner.andThen {
      new LiftKind[To, OptionT[To, *]] {
        def liftF[A](value: To[A]): OptionT[To, A] = OptionT.liftF(value)
        def limitedMapK[A](value: OptionT[To, A])(scope: To ~> To): OptionT[To, A] =
          value.mapK(scope)
      }
    }

  implicit def writerT[From[_], To[_]: Applicative, L: Monoid](
      implicit inner: LiftKind[From, To]
  ): LiftKind[From, WriterT[To, L, *]] =
    inner.andThen {
      new LiftKind[To, WriterT[To, L, *]] {
        def liftF[A](value: To[A]): WriterT[To, L, A] = WriterT.liftF(value)
        def limitedMapK[A](value: WriterT[To, L, A])(scope: To ~> To): WriterT[To, L, A] =
          value.mapK(scope)
      }
    }
}
