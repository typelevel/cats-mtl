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
 * Lifts values and scope transformations from the higher-kinded type `F` to the higher-kinded
 * type `G`.
 */
@implicitNotFound("no way defined to lift values and scopes from ${F} to ${G}")
trait LiftKind[F[_], G[_]] extends LiftValue[F, G] with Serializable {

  /**
   * Modifies the context of a `G[A]` using the given scope transformation in `F`.
   *
   * @note
   *   This method is usually best implemented by a `mapK` method on `G`.
   */
  def limitedMapK[A](ga: G[A])(scope: F ~> F): G[A]

  /**
   * Lifts a scope transformation in `F` into a scope operation in `G`.
   *
   * @note
   *   Implementors SHOULD NOT override this method; the only reason it is not final is for
   *   optimization of the identity case.
   */
  def liftScope(scope: F ~> F): G ~> G =
    new (G ~> G) {
      def apply[A](fa: G[A]): G[A] = limitedMapK(fa)(scope)
    }

  /** @return an instance that lifts `E` to `F` and then `F` to `G` */
  def compose[E[_]](that: LiftKind[E, F]): LiftKind[E, G] =
    LiftKind.Composed(that, this)

  /** @return an instance that lifts `F` to `G` and then `G` to `H` */
  def andThen[H[_]](that: LiftKind[G, H]): LiftKind[F, H] =
    LiftKind.Composed(this, that)
}

object LiftKind extends LowPriorityLiftKindInstances {

  private[this] final class Identity[F[_]: Applicative]
      extends LiftValue.Identity[F]
      with LiftKind[F, F] {
    def limitedMapK[A](ga: F[A])(scope: F ~> F): F[A] = scope(ga)
    override def liftScope(scope: F ~> F): F ~> F = scope
  }

  private[this] final class Composed[F[_], G[_], H[_]] private (
      inner: LiftKind[F, G],
      outer: LiftKind[G, H]
  ) extends LiftKind[F, H] {
    def applicativeF: Applicative[F] = inner.applicativeF
    def applicativeG: Applicative[H] = outer.applicativeG
    def apply[A](fa: F[A]): H[A] = outer(inner(fa))
    def limitedMapK[A](ga: H[A])(scope: F ~> F): H[A] =
      outer.limitedMapK(ga)(inner.liftScope(scope))
    override def liftScope(scope: F ~> F): H ~> H =
      outer.liftScope(inner.liftScope(scope))
  }

  private object Composed {
    def apply[F[_], G[_], H[_]](
        inner: LiftKind[F, G],
        outer: LiftKind[G, H]
    ): LiftKind[F, H] =
      if (inner.isInstanceOf[Identity[F @uc]]) outer.asInstanceOf[LiftKind[F, H]]
      else if (outer.isInstanceOf[Identity[G @uc]]) inner.asInstanceOf[LiftKind[F, H]]
      else new Composed(inner, outer)
  }

  def apply[F[_], G[_]](implicit lk: LiftKind[F, G]): LiftKind[F, G] = lk

  implicit def id[F[_]: Applicative]: LiftKind[F, F] = new Identity[F]

  /*
   The compositional implicits are sufficient to derive all implicit instances,
   but non-compositional ones with higher priority are provided to avoid
   unnecessary allocation.
   */

  implicit def eitherT[F[_], L](implicit F: Monad[F]): LiftKind[F, EitherT[F, L, *]] =
    eitherTImpl(F)

  implicit def iorT[F[_], L: Semigroup](implicit F: Monad[F]): LiftKind[F, IorT[F, L, *]] =
    iorTImpl(F)

  implicit def kleisli[F[_], A](implicit F: Applicative[F]): LiftKind[F, Kleisli[F, A, *]] =
    kleisliImpl(F)

  implicit def optionT[F[_]](implicit F: Monad[F]): LiftKind[F, OptionT[F, *]] =
    optionTImpl(F)

  implicit def writerT[F[_], L: Monoid](
      implicit F: Applicative[F]
  ): LiftKind[F, WriterT[F, L, *]] =
    writerTImpl(F)
}

private[mtl] sealed trait LowPriorityLiftKindInstances {
  protected[this] def eitherTImpl[F[_], L](F: Monad[F]): LiftKind[F, EitherT[F, L, *]] =
    new LiftKind[F, EitherT[F, L, *]] {
      implicit val applicativeF: Monad[F] = F
      val applicativeG: Applicative[EitherT[F, L, *]] = EitherT.catsDataMonadErrorForEitherT
      def apply[A](fa: F[A]): EitherT[F, L, A] = EitherT.liftF(fa)
      def limitedMapK[A](ga: EitherT[F, L, A])(scope: F ~> F): EitherT[F, L, A] =
        ga.mapK(scope)
    }

  implicit def eitherTComposed[F[_], G[_], L](
      implicit inner: LiftKind[F, G],
      G: Monad[G]
  ): LiftKind[F, EitherT[G, L, *]] =
    inner.andThen(eitherTImpl(G))

  protected[this] def iorTImpl[F[_], L: Semigroup](F: Monad[F]): LiftKind[F, IorT[F, L, *]] =
    new LiftKind[F, IorT[F, L, *]] {
      implicit val applicativeF: Monad[F] = F
      val applicativeG: Applicative[IorT[F, L, *]] = IorT.catsDataMonadErrorForIorT
      def apply[A](fa: F[A]): IorT[F, L, A] = IorT.liftF(fa)
      def limitedMapK[A](ga: IorT[F, L, A])(scope: F ~> F): IorT[F, L, A] =
        ga.mapK(scope)
    }

  implicit def iorTComposed[F[_], G[_], L: Semigroup](
      implicit inner: LiftKind[F, G],
      G: Monad[G]
  ): LiftKind[F, IorT[G, L, *]] =
    inner.andThen(iorTImpl(G))

  protected[this] def kleisliImpl[F[_], A](F: Applicative[F]): LiftKind[F, Kleisli[F, A, *]] =
    new LiftKind[F, Kleisli[F, A, *]] {
      implicit val applicativeF: Applicative[F] = F
      val applicativeG: Applicative[Kleisli[F, A, *]] = Kleisli.catsDataApplicativeForKleisli
      def apply[B](fa: F[B]): Kleisli[F, A, B] = Kleisli.liftF(fa)
      def limitedMapK[B](ga: ReaderT[F, A, B])(scope: F ~> F): ReaderT[F, A, B] =
        ga.mapK(scope)
    }

  implicit def kleisliComposed[F[_], G[_], A](
      implicit inner: LiftKind[F, G]
  ): LiftKind[F, Kleisli[G, A, *]] =
    inner.andThen(kleisliImpl(inner.applicativeG))

  protected[this] def optionTImpl[F[_]](F: Monad[F]): LiftKind[F, OptionT[F, *]] =
    new LiftKind[F, OptionT[F, *]] {
      implicit val applicativeF: Monad[F] = F
      val applicativeG: Applicative[OptionT[F, *]] = OptionT.catsDataMonadForOptionT
      def apply[A](fa: F[A]): OptionT[F, A] = OptionT.liftF(fa)
      def limitedMapK[A](ga: OptionT[F, A])(scope: F ~> F): OptionT[F, A] =
        ga.mapK(scope)
    }

  implicit def optionTComposed[F[_], G[_]](
      implicit inner: LiftKind[F, G],
      G: Monad[G]
  ): LiftKind[F, OptionT[G, *]] =
    inner.andThen(optionTImpl(G))

  protected[this] def writerTImpl[F[_], L: Monoid](
      F: Applicative[F]
  ): LiftKind[F, WriterT[F, L, *]] =
    new LiftKind[F, WriterT[F, L, *]] {
      implicit val applicativeF: Applicative[F] = F
      val applicativeG: Applicative[WriterT[F, L, *]] = WriterT.catsDataApplicativeForWriterT
      def apply[A](fa: F[A]): WriterT[F, L, A] = WriterT.liftF(fa)
      def limitedMapK[A](ga: WriterT[F, L, A])(scope: F ~> F): WriterT[F, L, A] =
        ga.mapK(scope)
    }

  implicit def writerTComposed[F[_], G[_], L: Monoid](
      implicit inner: LiftKind[F, G]
  ): LiftKind[F, WriterT[G, L, *]] =
    inner.andThen(writerTImpl(inner.applicativeG))
}
