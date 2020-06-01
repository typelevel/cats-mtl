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

@implicitNotFound(
  "Could not find an implicit instance of ApplicativeHandle[${F}, ${E}]. If you\nhave a good way of handling errors of type ${E} at this location, you may want\nto construct a value of type EitherT for this call-site, rather than ${F}.\nAn example type:\n\n  EitherT[${F}, ${E}, *]\n\nThis is analogous to writing try/catch around this call. The EitherT will\n\"catch\" the errors of type ${E}.\n\nIf you do not wish to handle errors of type ${E} at this location, you should\nadd an implicit parameter of this type to your function. For example:\n\n  (implicit fhandle: ApplicativeHandle[${F}, ${E}}])\n")
trait ApplicativeHandle[F[_], E] extends FunctorRaise[F, E] with Serializable {
  def applicative: Applicative[F]

  def handleWith[A](fa: F[A])(f: E => F[A]): F[A]

  def attempt[A](fa: F[A]): F[Either[E, A]] =
    handleWith(applicative.map(fa)(Right(_): Either[E, A]))(e => applicative.pure(Left(e)))

  def attemptT[A](fa: F[A]): EitherT[F, E, A] =
    EitherT(attempt(fa))

  def handle[A](fa: F[A])(f: E => A): F[A] =
    handleWith(fa)(e => applicative.pure(f(e)))
}

private[mtl] trait ApplicativeHandleInstances {

  implicit final def handleEitherT[M[_], E](
      implicit M: Monad[M]): ApplicativeHandle[EitherTC[M, E]#l, E] = {
    new ApplicativeHandle[EitherTC[M, E]#l, E] {
      val applicative: Applicative[EitherTC[M, E]#l] = EitherT.catsDataMonadErrorForEitherT(M)

      val functor: Functor[EitherTC[M, E]#l] = EitherT.catsDataFunctorForEitherT(M)

      def raise[A](e: E): EitherT[M, E, A] = EitherT(M.pure(Left(e)))

      def handleWith[A](fa: EitherT[M, E, A])(f: E => EitherT[M, E, A]): EitherT[M, E, A] =
        EitherT(M.flatMap(fa.value) {
          case Left(e) => f(e).value
          case r @ Right(_) => M.pure(r)
        })
    }
  }

  implicit final def handleEither[E]: ApplicativeHandle[EitherC[E]#l, E] = {
    new ApplicativeHandle[EitherC[E]#l, E] {
      val applicative: Applicative[EitherC[E]#l] =
        cats.instances.either.catsStdInstancesForEither[E]

      val functor: Functor[EitherC[E]#l] = cats.instances.either.catsStdInstancesForEither

      def raise[A](e: E): Either[E, A] = Left(e)

      def handleWith[A](fa: Either[E, A])(f: E => Either[E, A]): Either[E, A] =
        fa match {
          case Left(e) => f(e)
          case r @ Right(_) => r
        }
    }
  }

  implicit final def handleOptionT[M[_]](
      implicit M: Monad[M]): ApplicativeHandle[OptionTC[M]#l, Unit] = {
    new ApplicativeHandle[OptionTC[M]#l, Unit] {
      val applicative: Applicative[OptionTC[M]#l] = OptionT.catsDataMonadForOptionT(M)

      val functor: Functor[OptionTC[M]#l] = OptionT.catsDataFunctorForOptionT(M)

      def raise[A](e: Unit): OptionT[M, A] = OptionT(M.pure(None))

      def handleWith[A](fa: OptionT[M, A])(f: Unit => OptionT[M, A]): OptionT[M, A] =
        OptionT(M.flatMap(fa.value) {
          case None => f(()).value
          case s @ Some(_) => M.pure(s)
        })
    }
  }

  implicit final def handleOption[E]: ApplicativeHandle[Option, Unit] = {
    new ApplicativeHandle[Option, Unit] {
      val applicative: Applicative[Option] = cats.instances.option.catsStdInstancesForOption

      val functor: Functor[Option] = cats.instances.option.catsStdInstancesForOption

      def raise[A](e: Unit): Option[A] = None

      def handleWith[A](fa: Option[A])(f: Unit => Option[A]): Option[A] =
        fa match {
          case None => f(())
          case s @ Some(_) => s
        }
    }
  }

  implicit final def handleValidated[E](
      implicit E: Semigroup[E]): ApplicativeHandle[Validated[E, ?], E] =
    new ApplicativeHandle[Validated[E, ?], E] {
      val applicative: Applicative[Validated[E, ?]] =
        Validated.catsDataApplicativeErrorForValidated[E]

      val functor: Functor[Validated[E, ?]] = Validated.catsDataApplicativeErrorForValidated[E]

      def raise[A](e: E): Validated[E, A] = Validated.Invalid(e)

      def handleWith[A](fa: Validated[E, A])(f: E => Validated[E, A]): Validated[E, A] =
        fa match {
          case Validated.Invalid(e) => f(e)
          case v @ Validated.Valid(_) => v
        }
    }

  implicit final def handleIorT[F[_], E](
      implicit E: Semigroup[E],
      F: Monad[F]): ApplicativeHandle[IorT[F, E, *], E] =
    new ApplicativeHandle[IorT[F, E, *], E] {
      val applicative: Applicative[IorT[F, E, ?]] = IorT.catsDataMonadErrorForIorT[F, E]

      val functor: Functor[IorT[F, E, ?]] = IorT.catsDataMonadErrorForIorT[F, E]

      def raise[A](e: E): IorT[F, E, A] = IorT.leftT(e)

      def handleWith[A](fa: IorT[F, E, A])(f: E => IorT[F, E, A]): IorT[F, E, A] =
        IorT(F.flatMap(fa.value) {
          case Ior.Left(e) => f(e).value
          case e @ _ => F.pure(e)
        })
    }

  implicit final def handleIor[E](implicit E: Semigroup[E]): ApplicativeHandle[Ior[E, *], E] =
    new ApplicativeHandle[Ior[E, *], E] {
      val applicative: Applicative[Ior[E, ?]] = Ior.catsDataMonadErrorForIor[E]

      val functor: Functor[Ior[E, ?]] = Ior.catsDataMonadErrorForIor[E]

      def raise[A](e: E): Ior[E, A] = Ior.Left(e)

      def handleWith[A](fa: Ior[E, A])(f: E => Ior[E, A]): Ior[E, A] =
        fa match {
          case Ior.Left(e) => f(e)
          case _ => fa
        }
    }

  implicit final def applicativeHandleKleisli[F[_], E, R](
      implicit F0: ApplicativeHandle[F, E],
      M: Monad[F]): ApplicativeHandle[Kleisli[F, R, *], E] =
    new ApplicativeHandle[Kleisli[F, R, *], E]
      with FunctorRaiseMonadPartialOrder[F, Kleisli[F, R, *], E] {
      val applicative: Applicative[Kleisli[F, R, *]] = Kleisli.catsDataMonadForKleisli[F, R]

      val F: FunctorRaise[F, E] = F0
      val lift: MonadPartialOrder[F, Kleisli[F, R, *]] =
        MonadPartialOrder.monadPartialOrderForKleisli[F, R]

      def handleWith[A](fa: Kleisli[F, R, A])(f: E => Kleisli[F, R, A]): Kleisli[F, R, A] =
        Kleisli(r => F0.handleWith(fa.run(r))(e => f(e).run(r)))
    }

  implicit final def applicativeHandleWriterT[F[_], E, L](
      implicit F0: ApplicativeHandle[F, E],
      M: Monad[F],
      L: Monoid[L]): ApplicativeHandle[WriterT[F, L, *], E] =
    new ApplicativeHandle[WriterT[F, L, *], E]
      with FunctorRaiseMonadPartialOrder[F, WriterT[F, L, *], E] {
      val applicative: Applicative[WriterT[F, L, *]] =
        WriterT.catsDataApplicativeForWriterT[F, L]

      val F: FunctorRaise[F, E] = F0
      val lift: MonadPartialOrder[F, WriterT[F, L, *]] =
        MonadPartialOrder.monadPartialOrderForWriterT[F, L]

      def handleWith[A](fa: WriterT[F, L, A])(f: E => WriterT[F, L, A]): WriterT[F, L, A] =
        WriterT(F0.handleWith(fa.run)(e => f(e).run))
    }

  implicit final def applicativeHandleStateT[F[_], E, S](
      implicit F0: ApplicativeHandle[F, E],
      M: Monad[F]): ApplicativeHandle[StateT[F, S, *], E] =
    new ApplicativeHandle[StateT[F, S, *], E]
      with FunctorRaiseMonadPartialOrder[F, StateT[F, S, *], E] {
      val applicative: Applicative[StateT[F, S, *]] =
        IndexedStateT.catsDataMonadForIndexedStateT[F, S]

      val F: FunctorRaise[F, E] = F0
      val lift: MonadPartialOrder[F, StateT[F, S, *]] =
        MonadPartialOrder.monadPartialOrderForStateT[F, S]

      def handleWith[A](fa: StateT[F, S, A])(f: E => StateT[F, S, A]): StateT[F, S, A] =
        StateT(s => F0.handleWith(fa.run(s))(e => f(e).run(s)))
    }

  implicit final def applicativeHandleRWST[F[_], E, R, L, S](
      implicit F0: ApplicativeHandle[F, E],
      M: Monad[F],
      L: Monoid[L]): ApplicativeHandle[RWST[F, R, L, S, *], E] =
    new ApplicativeHandle[RWST[F, R, L, S, *], E]
      with FunctorRaiseMonadPartialOrder[F, RWST[F, R, L, S, *], E] {
      val applicative: Applicative[RWST[F, R, L, S, *]] =
        IndexedReaderWriterStateT.catsDataMonadForRWST

      val F: FunctorRaise[F, E] = F0
      val lift: MonadPartialOrder[F, RWST[F, R, L, S, *]] =
        MonadPartialOrder.monadPartialOrderForRWST

      def handleWith[A](fa: RWST[F, R, L, S, A])(
          f: E => RWST[F, R, L, S, A]): RWST[F, R, L, S, A] =
        RWST { case (r, s) => F0.handleWith(fa.run(r, s))(e => f(e).run(r, s)) }
    }
}

object ApplicativeHandle extends ApplicativeHandleInstances {
  def apply[F[_], E](implicit ev: ApplicativeHandle[F, E]): ApplicativeHandle[F, E] = ev
}
