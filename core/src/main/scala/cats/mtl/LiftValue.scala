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

import cats.data.{ReaderWriterStateT => RWST, _}

import scala.annotation.implicitNotFound
import scala.{unchecked => uc}

/** Lifts values from the higher-kinded type `F` to the higher-kinded type `G`. */
@implicitNotFound("no way defined to lift values from ${F} to ${G}")
trait LiftValue[F[_], G[_]] extends (F ~> G) {

  /** An [[cats.Applicative `Applicative`]] instance for the higher-kinded type `F`. */
  def applicativeF: Applicative[F]

  /** An [[cats.Applicative `Applicative`]] instance for the higher-kinded type `G`. */
  def applicativeG: Applicative[G]

  /**
   * Lifts `F[A]` to `G[A]`.
   *
   * @note
   *   This method is usually best implemented by a `liftF` method on `G`'s companion object.
   */
  def apply[A](fa: F[A]): G[A]

  /** @return an instance that lifts `E` to `F` and then `F` to `G` */
  def compose[E[_]](that: LiftValue[E, F]): LiftValue[E, G] =
    LiftValue.Composed(that, this)

  /** @return an instance that lifts `F` to `G` and then `G` to `H` */
  def andThen[H[_]](that: LiftValue[G, H]): LiftValue[F, H] =
    LiftValue.Composed(this, that)
}

object LiftValue extends LowPriorityLiftValueInstances {

  private[mtl] class Identity[F[_]](implicit val applicative: Applicative[F])
      extends LiftValue[F, F] {
    final def applicativeF: Applicative[F] = applicative
    final def applicativeG: Applicative[F] = applicative
    final def apply[A](fa: F[A]): F[A] = fa
  }

  private[this] final class Composed[F[_], G[_], H[_]] private (
      inner: LiftValue[F, G],
      outer: LiftValue[G, H]
  ) extends LiftValue[F, H] {
    def applicativeF: Applicative[F] = inner.applicativeF
    def applicativeG: Applicative[H] = outer.applicativeG
    def apply[A](fa: F[A]): H[A] = outer(inner(fa))
  }

  private object Composed {
    def apply[F[_], G[_], H[_]](
        inner: LiftValue[F, G],
        outer: LiftValue[G, H]
    ): LiftValue[F, H] =
      if (inner.isInstanceOf[Identity[F @uc]]) outer.asInstanceOf[LiftValue[F, H]]
      else if (outer.isInstanceOf[Identity[G @uc]]) inner.asInstanceOf[LiftValue[F, H]]
      else new Composed(inner, outer)
  }

  def apply[F[_], G[_]](implicit lv: LiftValue[F, G]): LiftValue[F, G] = lv

  implicit def id[F[_]: Applicative]: LiftValue[F, F] = new Identity[F]

  /*
   The compositional implicits are sufficient to derive all implicit instances,
   but non-compositional ones with higher priority are provided to avoid
   unnecessary allocation.
   */

  implicit def eitherT[F[_], L](implicit F: Monad[F]): LiftValue[F, EitherT[F, L, *]] =
    eitherTImpl(F)

  implicit def iorT[F[_], L: Semigroup](implicit F: Monad[F]): LiftValue[F, IorT[F, L, *]] =
    iorTImpl(F)

  implicit def kleisli[F[_], A](implicit F: Applicative[F]): LiftValue[F, Kleisli[F, A, *]] =
    kleisliImpl(F)

  implicit def optionT[F[_]](implicit F: Monad[F]): LiftValue[F, OptionT[F, *]] =
    optionTImpl(F)

  implicit def rwst[F[_], E, L: Monoid, S](
      implicit F: Monad[F]
  ): LiftValue[F, RWST[F, E, L, S, *]] =
    rwstImpl(F)

  implicit def stateT[F[_], S](implicit F: Monad[F]): LiftValue[F, StateT[F, S, *]] =
    stateTImpl(F)

  implicit def writerT[F[_], L: Monoid](
      implicit F: Applicative[F]
  ): LiftValue[F, WriterT[F, L, *]] =
    writerTImpl(F)
}

private[mtl] sealed trait LowPriorityLiftValueInstances {
  protected[this] def eitherTImpl[F[_], L](F: Monad[F]): LiftValue[F, EitherT[F, L, *]] =
    new LiftValue[F, EitherT[F, L, *]] {
      implicit val applicativeF: Monad[F] = F
      val applicativeG: Applicative[EitherT[F, L, *]] = EitherT.catsDataMonadErrorForEitherT
      def apply[A](fa: F[A]): EitherT[F, L, A] = EitherT.liftF(fa)
    }

  implicit def eitherTComposed[F[_], G[_], L](
      implicit inner: LiftValue[F, G],
      G: Monad[G]
  ): LiftValue[F, EitherT[G, L, *]] =
    inner.andThen(eitherTImpl(G))

  protected[this] def iorTImpl[F[_], L: Semigroup](F: Monad[F]): LiftValue[F, IorT[F, L, *]] =
    new LiftValue[F, IorT[F, L, *]] {
      implicit val applicativeF: Monad[F] = F
      val applicativeG: Applicative[IorT[F, L, *]] = IorT.catsDataMonadErrorForIorT
      def apply[A](fa: F[A]): IorT[F, L, A] = IorT.liftF(fa)
    }

  implicit def iorTComposed[F[_], G[_], L: Semigroup](
      implicit inner: LiftValue[F, G],
      G: Monad[G]
  ): LiftValue[F, IorT[G, L, *]] =
    inner.andThen(iorTImpl(G))

  protected[this] def kleisliImpl[F[_], A](F: Applicative[F]): LiftValue[F, Kleisli[F, A, *]] =
    new LiftValue[F, Kleisli[F, A, *]] {
      implicit val applicativeF: Applicative[F] = F
      val applicativeG: Applicative[Kleisli[F, A, *]] = Kleisli.catsDataApplicativeForKleisli
      def apply[B](fa: F[B]): Kleisli[F, A, B] = Kleisli.liftF(fa)
    }

  implicit def kleisliComposed[F[_], G[_], A](
      implicit inner: LiftValue[F, G]
  ): LiftValue[F, Kleisli[G, A, *]] =
    inner.andThen(kleisliImpl(inner.applicativeG))

  protected[this] def optionTImpl[F[_]](F: Monad[F]): LiftValue[F, OptionT[F, *]] =
    new LiftValue[F, OptionT[F, *]] {
      implicit val applicativeF: Monad[F] = F
      val applicativeG: Applicative[OptionT[F, *]] = OptionT.catsDataMonadForOptionT
      def apply[A](fa: F[A]): OptionT[F, A] = OptionT.liftF(fa)
    }

  implicit def optionTComposed[F[_], G[_]](
      implicit inner: LiftValue[F, G],
      G: Monad[G]
  ): LiftValue[F, OptionT[G, *]] =
    inner.andThen(optionTImpl(G))

  protected[this] def rwstImpl[F[_], E, L: Monoid, S](
      F: Monad[F]
  ): LiftValue[F, RWST[F, E, L, S, *]] =
    new LiftValue[F, RWST[F, E, L, S, *]] {
      implicit val applicativeF: Monad[F] = F
      val applicativeG: Applicative[RWST[F, E, L, S, *]] =
        IndexedReaderWriterStateT.catsDataMonadForRWST
      def apply[A](fa: F[A]): RWST[F, E, L, S, A] = RWST.liftF(fa)
    }

  implicit def rwstComposed[F[_], G[_], E, L: Monoid, S](
      implicit inner: LiftValue[F, G],
      G: Monad[G]
  ): LiftValue[F, RWST[G, E, L, S, *]] =
    inner.andThen(rwstImpl(G))

  protected[this] def stateTImpl[F[_], S](F: Monad[F]): LiftValue[F, StateT[F, S, *]] =
    new LiftValue[F, StateT[F, S, *]] {
      implicit val applicativeF: Monad[F] = F
      val applicativeG: Applicative[StateT[F, S, *]] =
        IndexedStateT.catsDataMonadForIndexedStateT
      def apply[A](fa: F[A]): StateT[F, S, A] = StateT.liftF(fa)
    }

  implicit def stateTComposed[F[_], G[_], S](
      implicit inner: LiftValue[F, G],
      G: Monad[G]
  ): LiftValue[F, StateT[G, S, *]] =
    inner.andThen(stateTImpl(G))

  protected[this] def writerTImpl[F[_], L: Monoid](
      F: Applicative[F]
  ): LiftValue[F, WriterT[F, L, *]] =
    new LiftValue[F, WriterT[F, L, *]] {
      implicit val applicativeF: Applicative[F] = F
      val applicativeG: Applicative[WriterT[F, L, *]] = WriterT.catsDataApplicativeForWriterT
      def apply[A](fa: F[A]): WriterT[F, L, A] = WriterT.liftF(fa)
    }

  implicit def writerTComposed[F[_], G[_], L: Monoid](
      implicit inner: LiftValue[F, G]
  ): LiftValue[F, WriterT[G, L, *]] =
    inner.andThen(writerTImpl(inner.applicativeG))
}
