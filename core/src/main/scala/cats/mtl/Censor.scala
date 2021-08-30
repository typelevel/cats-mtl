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

import cats.data.{
  EitherT,
  IndexedReaderWriterStateT,
  IndexedStateT,
  IorT,
  Kleisli,
  OptionT,
  ReaderWriterStateT => RWST,
  StateT,
  WriterT
}
import cats.syntax.all._

import scala.annotation.implicitNotFound

@implicitNotFound(
  "Could not find an implicit instance of Censor[${F}, ${L}]. If you wish\nto capture side-channel output of type ${L} at this location, you may want\nto construct a value of type WriterT for this call-site, rather than ${F}.\nAn example type:\n\n  WriterT[${F}, ${L}, *]\n\nOne use-case for this would be if ${L} represents an accumulation of values\nwhich are produced by this function *in addition to* its normal results.\nThis can be used to implement some forms of pure logging.\n\nIf you do not wish to capture a side-channel of type ${L} at this location,\nyou should add an implicit parameter of this type to your function. For\nexample:\n\n  (implicit fcensor: Censor[${F}, ${L}])\n")
trait Censor[F[_], L] extends Listen[F, L] {
  val applicative: Applicative[F]
  val monoid: Monoid[L]
  override final def functor: Functor[F] = applicative

  def censor[A](fa: F[A])(f: L => L): F[A]

  def clear[A](fa: F[A]): F[A] = censor(fa)(_ => monoid.empty)
}

object Censor extends CensorInstances {
  def apply[F[_], L](implicit ev: Censor[F, L]): Censor[F, L] = ev
}

private[mtl] trait LowPriorityCensorInstances {
  implicit final def inductiveCensorWriterT[M[_]: Applicative, L0: Monoid, L: Monoid](
      implicit A: Censor[M, L]): Censor[WriterT[M, L0, *], L] =
    new ListenInductiveWriterT[M, L0, L] with Censor[WriterT[M, L0, *], L] {
      val applicative: Applicative[WriterT[M, L0, *]] = WriterT.catsDataApplicativeForWriterT
      val monoid: Monoid[L] = Monoid[L]

      def censor[A](fa: WriterT[M, L0, A])(f: L => L): WriterT[M, L0, A] =
        WriterT(A.censor(fa.run)(f))
    }

  implicit final def inductiveCensorRWST[M[_]: Monad, R, L0: Monoid, L: Monoid, S](
      implicit A: Censor[M, L]): Censor[RWST[M, R, L0, S, *], L] =
    new ListenInductiveRWST[M, R, L0, L, S] with Censor[RWST[M, R, L0, S, *], L] {
      val applicative: Applicative[RWST[M, R, L0, S, *]] =
        IndexedReaderWriterStateT.catsDataMonadForRWST
      val monoid: Monoid[L] = Monoid[L]

      def censor[A](fa: RWST[M, R, L0, S, A])(f: L => L): RWST[M, R, L0, S, A] =
        RWST { case (r, s) => A.censor(fa.run(r, s))(f) }
    }
}

private[mtl] trait CensorInstances extends LowPriorityCensorInstances {
  implicit final def censorWriterT[M[_], L](
      implicit M: Applicative[M],
      L: Monoid[L]): Censor[WriterT[M, L, *], L] =
    new ListenWriterT[M, L] with Censor[WriterT[M, L, *], L] {
      val applicative: Applicative[WriterT[M, L, *]] =
        cats.data.WriterT.catsDataApplicativeForWriterT[M, L]

      val monoid: Monoid[L] = L

      override def clear[A](fa: WriterT[M, L, A]): WriterT[M, L, A] =
        WriterT(fa.value.tupleLeft(L.empty))

      def censor[A](fa: WriterT[M, L, A])(f: L => L): WriterT[M, L, A] =
        WriterT(fa.run.map { case (l, a) => (f(l), a) })

    }

  implicit final def censorRWST[M[_], R, L, S](
      implicit L: Monoid[L],
      M: Monad[M]): Censor[RWST[M, R, L, S, *], L] =
    new ListenRWST[M, R, L, S] with Censor[RWST[M, R, L, S, *], L] {
      val applicative: Applicative[RWST[M, R, L, S, *]] =
        IndexedReaderWriterStateT.catsDataMonadForRWST

      val monoid: Monoid[L] = L

      def censor[A](faf: RWST[M, R, L, S, A])(f: L => L): RWST[M, R, L, S, A] =
        RWST((e, s) => faf.run(e, s).map { case (l, s, a) => (f(l), s, a) })

    }

  implicit final def censorKleisli[F[_], R, L](
      implicit L: Monoid[L],
      A: Censor[F, L],
      F: Applicative[F]): Censor[Kleisli[F, R, *], L] =
    new ListenKleisli[F, R, L] with Censor[Kleisli[F, R, *], L] {
      val applicative: Applicative[Kleisli[F, R, *]] = Kleisli.catsDataApplicativeForKleisli
      val monoid: cats.Monoid[L] = L

      def censor[A](fa: Kleisli[F, R, A])(f: L => L): Kleisli[F, R, A] =
        Kleisli(r => A.censor(fa.run(r))(f))
    }

  implicit final def censorStateT[F[_], S, L](
      implicit L: Monoid[L],
      A: Censor[F, L],
      F: Monad[F]): Censor[StateT[F, S, *], L] =
    new ListenStateT[F, S, L] with Censor[StateT[F, S, *], L] {
      val applicative: Applicative[StateT[F, S, *]] =
        IndexedStateT.catsDataMonadForIndexedStateT
      val monoid: cats.Monoid[L] = L

      def censor[A](fa: StateT[F, S, A])(f: L => L): StateT[F, S, A] =
        StateT(s => A.censor(fa.run(s))(f))
    }

  implicit final def censorEitherT[F[_], E, L](
      implicit L: Monoid[L],
      A: Censor[F, L],
      F: Monad[F]): Censor[EitherT[F, E, *], L] =
    new ListenEitherT[F, E, L] with Censor[EitherT[F, E, *], L] {
      val applicative: Applicative[EitherT[F, E, *]] = EitherT.catsDataMonadErrorForEitherT
      val monoid: cats.Monoid[L] = L

      def censor[A](fa: EitherT[F, E, A])(f: L => L): EitherT[F, E, A] =
        EitherT(A.censor(fa.value)(f))
    }

  implicit final def censorIorT[F[_], E, L](
      implicit L: Monoid[L],
      A: Censor[F, L],
      F: Monad[F],
      E: Semigroup[E]): Censor[IorT[F, E, *], L] =
    new ListenIorT[F, E, L] with Censor[IorT[F, E, *], L] {
      val applicative: Applicative[IorT[F, E, *]] = IorT.catsDataMonadErrorForIorT
      val monoid: cats.Monoid[L] = L

      def censor[A](fa: IorT[F, E, A])(f: L => L): IorT[F, E, A] =
        IorT(A.censor(fa.value)(f))
    }

  implicit final def censorOptionT[F[_], L](
      implicit L: Monoid[L],
      A: Censor[F, L],
      F: Monad[F]): Censor[OptionT[F, *], L] =
    new ListenOptionT[F, L] with Censor[OptionT[F, *], L] {
      val applicative: Applicative[OptionT[F, *]] = OptionT.catsDataMonadForOptionT
      val monoid: cats.Monoid[L] = L

      def censor[A](fa: OptionT[F, A])(f: L => L): OptionT[F, A] =
        OptionT(A.censor(fa.value)(f))
    }

}
