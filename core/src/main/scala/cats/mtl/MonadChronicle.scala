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

import cats.data.{ReaderWriterStateT => RWST, IndexedReaderWriterStateT => IndexedRWST, _}
import cats.implicits._
import cats.data.Ior.Both

trait MonadChronicle[F[_], E] extends Serializable {
  val monad: Monad[F]

  def dictate(c: E): F[Unit]

  def disclose[A](c: E)(implicit M: Monoid[A]): F[A] = monad.as(dictate(c), M.empty)

  def confess[A](c: E): F[A]

  def materialize[A](fa: F[A]): F[E Ior A]

  def memento[A](fa: F[A]): F[Either[E, A]] =
    monad.flatMap(materialize(fa)) {
      case Ior.Left(e) => monad.pure(Left(e))
      case Ior.Right(a) => monad.pure(Right(a))
      case Ior.Both(e, a) => monad.as(dictate(e), Right(a))
    }

  def absolve[A](fa: F[A])(a: => A): F[A] =
    monad.map(materialize(fa)) {
      case Ior.Left(_) => a
      case Ior.Right(a0) => a0
      case Ior.Both(_, a0) => a0
    }

  def condemn[A](fa: F[A]): F[A] =
    monad.flatMap(materialize(fa)) {
      case Ior.Left(e) => confess(e)
      case Ior.Right(a) => monad.pure(a)
      case Ior.Both(e, _) => confess(e)
    }

  def retcon[A](fa: F[A])(cc: E => E): F[A] =
    monad.flatMap(materialize(fa)) {
      case Ior.Left(e) => confess(cc(e))
      case Ior.Right(a) => monad.pure(a)
      case Ior.Both(e, a) => monad.as(dictate(cc(e)), a)
    }

  def chronicle[A](ior: E Ior A): F[A] =
    ior match {
      case Ior.Left(e) => confess(e)
      case Ior.Right(a) => monad.pure(a)
      case Ior.Both(e, a) => monad.as(dictate(e), a)
    }
}

private[mtl] trait MonadChronicleInstances {

  implicit def monadChronicleForIorT[F[_], E: Semigroup](
      implicit F: Monad[F]): MonadChronicle[IorT[F, E, *], E] =
    new MonadChronicle[IorT[F, E, *], E] {
      override val monad: Monad[IorT[F, E, *]] = IorT.catsDataMonadErrorForIorT

      override def dictate(c: E): IorT[F, E, Unit] = IorT.bothT[F](c, ())

      override def confess[A](c: E): IorT[F, E, A] = IorT.leftT[F, A](c)

      override def materialize[A](fa: IorT[F, E, A]): IorT[F, E, E Ior A] =
        IorT[F, E, E Ior A] {
          F.map(fa.value) {
            case Ior.Left(e) => Ior.right(Ior.left(e))
            case Ior.Right(a) => Ior.right(Ior.right(a))
            case Ior.Both(e, a) => Ior.right(Ior.both(e, a))
          }
        }
    }

  implicit final def chronicleIor[E](implicit S: Semigroup[E]): MonadChronicle[Ior[E, *], E] =
    new MonadChronicle[Ior[E, *], E] {
      override val monad: Monad[Ior[E, *]] = Ior.catsDataMonadErrorForIor

      override def dictate(c: E): Ior[E, Unit] = Ior.both(c, ())

      override def confess[A](c: E): Ior[E, A] = Ior.left(c)

      override def materialize[A](fa: Ior[E, A]): Ior[E, Ior[E, A]] =
        fa match {
          case Ior.Left(e) => Ior.right(Ior.left(e))
          case Ior.Right(a) => Ior.right(Ior.right(a))
          case Ior.Both(e, a) => Ior.right(Ior.both(e, a))
        }
    }

  implicit def monadChronicleForWriterT[F[_]: Monad, E, L: Monoid](
      implicit F: MonadChronicle[F, E]): MonadChronicle[WriterT[F, L, *], E] =
    new MonadChronicle[WriterT[F, L, *], E] {
      val monad: Monad[WriterT[F, L, *]] = WriterT.catsDataMonadForWriterT[F, L]
      def confess[A](c: E): WriterT[F, L, A] = WriterT.liftF(F.confess[A](c))
      def dictate(c: E): WriterT[F, L, Unit] = WriterT.liftF(F.dictate(c))
      def materialize[A](fa: WriterT[F, L, A]): WriterT[F, L, Ior[E, A]] =
        WriterT(F.materialize(fa.run).map(_.sequence))
    }

  implicit def monadChronicleForEitherT[F[_]: Monad, E, E2](
      implicit F: MonadChronicle[F, E]): MonadChronicle[EitherT[F, E2, *], E] =
    new MonadChronicle[EitherT[F, E2, *], E] {
      val monad: Monad[EitherT[F, E2, *]] = EitherT.catsDataMonadErrorForEitherT[F, E2]
      def confess[A](c: E): EitherT[F, E2, A] = EitherT.liftF(F.confess[A](c))
      def dictate(c: E): EitherT[F, E2, Unit] = EitherT.liftF(F.dictate(c))
      def materialize[A](fa: EitherT[F, E2, A]): EitherT[F, E2, Ior[E, A]] =
        EitherT(F.materialize(fa.value).map(_.sequence))
    }

  implicit def monadChronicleForKleisli[F[_]: Monad, E, R](
      implicit F: MonadChronicle[F, E]): MonadChronicle[Kleisli[F, R, *], E] =
    new MonadChronicle[Kleisli[F, R, *], E] {
      val monad: Monad[Kleisli[F, R, *]] = Kleisli.catsDataMonadForKleisli[F, R]
      def confess[A](c: E): Kleisli[F, R, A] = Kleisli.liftF(F.confess[A](c))
      def dictate(c: E): Kleisli[F, R, Unit] = Kleisli.liftF(F.dictate(c))
      def materialize[A](fa: Kleisli[F, R, A]): Kleisli[F, R, Ior[E, A]] =
        Kleisli(r => F.materialize(fa.run(r)))
    }

  implicit def monadChronicleForStateT[F[_]: Monad, E, S](
      implicit F: MonadChronicle[F, E]): MonadChronicle[StateT[F, S, *], E] =
    new MonadChronicle[StateT[F, S, *], E] {
      val monad: Monad[StateT[F, S, *]] = IndexedStateT.catsDataMonadForIndexedStateT[F, S]
      def confess[A](c: E): StateT[F, S, A] = StateT.liftF(F.confess[A](c))
      def dictate(c: E): StateT[F, S, Unit] = StateT.liftF(F.dictate(c))
      def materialize[A](fa: StateT[F, S, A]): StateT[F, S, Ior[E, A]] =
        StateT(s =>
          F.materialize(fa.run(s)).map {
            case Both(e, (s2, a)) => (s2, Both(e, a))
            case Ior.Left(e) => (s, Ior.Left(e))
            case Ior.Right((s2, a)) => (s2, Ior.Right(a))
          })
    }

  implicit def monadChronicleForOptionT[F[_]: Monad, E](
      implicit F: MonadChronicle[F, E]): MonadChronicle[OptionT[F, *], E] =
    new MonadChronicle[OptionT[F, *], E] {
      val monad: Monad[OptionT[F, *]] = OptionT.catsDataMonadForOptionT[F]
      def confess[A](c: E): OptionT[F, A] = OptionT.liftF(F.confess[A](c))
      def dictate(c: E): OptionT[F, Unit] = OptionT.liftF(F.dictate(c))
      def materialize[A](fa: OptionT[F, A]): OptionT[F, Ior[E, A]] =
        OptionT(F.materialize(fa.value).map(_.sequence))
    }

  implicit def monadChronicleForRWST[F[_]: Monad, E, R, L: Monoid, S](
      implicit F: MonadChronicle[F, E]): MonadChronicle[RWST[F, R, L, S, *], E] =
    new MonadChronicle[RWST[F, R, L, S, *], E] {
      val monad: Monad[RWST[F, R, L, S, *]] = IndexedRWST.catsDataMonadForRWST[F, R, L, S]
      def confess[A](c: E): RWST[F, R, L, S, A] = RWST.liftF(F.confess[A](c))
      def dictate(c: E): RWST[F, R, L, S, Unit] = RWST.liftF(F.dictate(c))
      def materialize[A](fa: RWST[F, R, L, S, A]): RWST[F, R, L, S, Ior[E, A]] =
        RWST((r, s) =>
          F.materialize(fa.run(r, s)).map {
            case Both(e, (l, s2, a)) => (l, s2, Both(e, a))
            case Ior.Left(e) => (Monoid[L].empty, s, Ior.Left(e))
            case Ior.Right((l, s2, a)) => (l, s2, Ior.Right(a))
          })
    }
}

object MonadChronicle extends MonadChronicleInstances {
  def dictate[F[_], E](e: E)(implicit ev: MonadChronicle[F, E]): F[Unit] = ev.dictate(e)

  def disclose[F[_], A, E](c: E)(implicit ev: MonadChronicle[F, E], m: Monoid[A]): F[A] =
    ev.disclose(c)

  def confess[F[_], E, A](c: E)(implicit ev: MonadChronicle[F, E]): F[A] = ev.confess(c)

  def materialize[F[_], E, A](fa: F[A])(implicit ev: MonadChronicle[F, E]): F[E Ior A] =
    ev.materialize(fa)

  def chronicle[F[_], E, A](ior: E Ior A)(implicit ev: MonadChronicle[F, E]): F[A] =
    ev.chronicle(ior)

  def apply[F[_], E](implicit ev: MonadChronicle[F, E]): MonadChronicle[F, E] =
    ev
}
