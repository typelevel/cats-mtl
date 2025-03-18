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
import cats.data.EitherT
import cats.data.IorT
import cats.data.Kleisli
import cats.data.OptionT
import cats.data.StateT
import cats.~>

/**
 * A utility for transforming the higher-kinded type `F` to another higher-kinded type `G`.
 */
@annotation.implicitNotFound("No transformer defined from ${F} to ${G}")
trait KindTransformer[F[_], G[_]] {

  /**
   * A higher-kinded function that lifts the kind `F` into a `G`.
   *
   * @note
   *   This method is usually best implemented by a `liftK` method on `G`'s companion object.
   */
  val liftK: F ~> G

  /**
   * Modify the context of `G[A]` using the natural transformation `f`.
   *
   * This method is "limited" in the sense that while most `mapK` methods can modify the context
   * using arbitrary transformations, this method can only modify the context using
   * transformations from `F` to `F`.
   *
   * @note
   *   This method is usually best implemented by a `mapK` method on `G`.
   */
  def limitedMapK[A](ga: G[A])(f: F ~> F): G[A]

  /**
   * Lifts a natural transformation from `F` to `F` into a natural transformation from `G` to
   * `G`.
   *
   * @note
   *   Implementors SHOULD NOT override this method; the only reason it is not final is for
   *   optimization of the identity case.
   */
  def liftFunctionK(f: F ~> F): G ~> G =
    new (G ~> G) {
      def apply[A](ga: G[A]): G[A] = limitedMapK(ga)(f)
    }
}

object KindTransformer {
  implicit def id[F[_]]: KindTransformer[F, F] =
    new KindTransformer[F, F] {
      val liftK: F ~> F = FunctionK.id
      def limitedMapK[A](ga: F[A])(f: F ~> F): F[A] = f(ga)
      override def liftFunctionK(f: F ~> F): F ~> F = f
    }

  implicit def optionT[F[_]: Functor]: KindTransformer[F, OptionT[F, *]] =
    new KindTransformer[F, OptionT[F, *]] {
      val liftK: F ~> OptionT[F, *] = OptionT.liftK
      def limitedMapK[A](ga: OptionT[F, A])(f: F ~> F): OptionT[F, A] =
        ga.mapK(f)
    }

  implicit def eitherT[F[_]: Functor, L]: KindTransformer[F, EitherT[F, L, *]] =
    new KindTransformer[F, EitherT[F, L, *]] {
      val liftK: F ~> EitherT[F, L, *] = EitherT.liftK
      def limitedMapK[R](ga: EitherT[F, L, R])(f: F ~> F): EitherT[F, L, R] =
        ga.mapK(f)
    }

  implicit def iorT[F[_]: Functor, L]: KindTransformer[F, IorT[F, L, *]] =
    new KindTransformer[F, IorT[F, L, *]] {
      val liftK: F ~> IorT[F, L, *] = IorT.liftK
      def limitedMapK[R](ga: IorT[F, L, R])(f: F ~> F): IorT[F, L, R] =
        ga.mapK(f)
    }

  implicit def kleisli[F[_], A]: KindTransformer[F, Kleisli[F, A, *]] =
    new KindTransformer[F, Kleisli[F, A, *]] {
      val liftK: F ~> Kleisli[F, A, *] = Kleisli.liftK
      def limitedMapK[B](ga: Kleisli[F, A, B])(f: F ~> F): Kleisli[F, A, B] =
        ga.mapK(f)
    }

  implicit def stateT[F[_]: Monad, S]: KindTransformer[F, StateT[F, S, *]] =
    new KindTransformer[F, StateT[F, S, *]] {
      val liftK: F ~> StateT[F, S, *] = StateT.liftK
      def limitedMapK[A](ga: StateT[F, S, A])(f: F ~> F): StateT[F, S, A] =
        ga.mapK(f)
    }
}
