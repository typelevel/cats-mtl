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
import scala.util.control.NoStackTrace

@implicitNotFound(
  "Could not find an implicit instance of Handle[${F}, ${E}]. If you\nhave a good way of handling errors of type ${E} at this location, you may want\nto construct a value of type EitherT for this call-site, rather than ${F}.\nAn example type:\n\n  EitherT[${F}, ${E}, *]\n\nThis is analogous to writing try/catch around this call. The EitherT will\n\"catch\" the errors of type ${E}.\n\nIf you do not wish to handle errors of type ${E} at this location, you should\nadd an implicit parameter of this type to your function. For example:\n\n  (implicit fhandle: Handle[${F}, ${E}}])\n")
trait Handle[F[_], E] extends Raise[F, E] with Serializable {
  def applicative: Applicative[F]

  override final def functor: Functor[F] = applicative

  def handleWith[A](fa: F[A])(f: E => F[A]): F[A]

  def attempt[A](fa: F[A]): F[Either[E, A]] =
    handleWith(applicative.map(fa)(Right(_): Either[E, A]))(e => applicative.pure(Left(e)))

  def attemptT[A](fa: F[A]): EitherT[F, E, A] =
    EitherT(attempt(fa))

  def handle[A](fa: F[A])(f: E => A): F[A] =
    handleWith(fa)(e => applicative.pure(f(e)))
}

private[mtl] trait HandleLowPriorityInstances {
  implicit final def handleForApplicativeError[F[_], E](
      implicit F: ApplicativeError[F, E]): Handle[F, E] =
    new Handle[F, E] {
      def applicative: Applicative[F] = F

      def raise[E2 <: E, A](e: E2): F[A] = F.raiseError(e)

      def handleWith[A](fa: F[A])(f: E => F[A]): F[A] = F.handleErrorWith(fa)(f)
    }
}

private[mtl] trait HandleInstances extends HandleLowPriorityInstances {

  implicit final def handleEitherT[M[_], E](
      implicit M: Monad[M]): Handle[EitherTC[M, E]#l, E] = {
    new Handle[EitherTC[M, E]#l, E] {
      val applicative: Applicative[EitherTC[M, E]#l] = EitherT.catsDataMonadErrorForEitherT(M)

      def raise[E2 <: E, A](e: E2): EitherT[M, E, A] = EitherT(M.pure(Left(e)))

      def handleWith[A](fa: EitherT[M, E, A])(f: E => EitherT[M, E, A]): EitherT[M, E, A] =
        EitherT(M.flatMap(fa.value) {
          case Left(e) => f(e).value
          case r @ Right(_) => M.pure(r)
        })
    }
  }

  implicit final def handleEither[E]: Handle[EitherC[E]#l, E] = {
    new Handle[EitherC[E]#l, E] {
      val applicative: Applicative[EitherC[E]#l] =
        cats.instances.either.catsStdInstancesForEither[E]

      def raise[E2 <: E, A](e: E2): Either[E, A] = Left(e)

      def handleWith[A](fa: Either[E, A])(f: E => Either[E, A]): Either[E, A] =
        fa match {
          case Left(e) => f(e)
          case r @ Right(_) => r
        }
    }
  }

  implicit final def handleOptionT[M[_]](implicit M: Monad[M]): Handle[OptionTC[M]#l, Unit] = {
    new Handle[OptionTC[M]#l, Unit] {
      val applicative: Applicative[OptionTC[M]#l] = OptionT.catsDataMonadForOptionT(M)

      def raise[E <: Unit, A](e: E): OptionT[M, A] = OptionT(M.pure(None))

      def handleWith[A](fa: OptionT[M, A])(f: Unit => OptionT[M, A]): OptionT[M, A] =
        OptionT(M.flatMap(fa.value) {
          case None => f(()).value
          case s @ Some(_) => M.pure(s)
        })
    }
  }

  implicit final def handleOption: Handle[Option, Unit] = {
    new Handle[Option, Unit] {
      val applicative: Applicative[Option] = cats.instances.option.catsStdInstancesForOption

      def raise[E <: Unit, A](e: E): Option[A] = None

      def handleWith[A](fa: Option[A])(f: Unit => Option[A]): Option[A] =
        fa match {
          case None => f(())
          case s @ Some(_) => s
        }
    }
  }

  implicit final def handleValidated[E](implicit E: Semigroup[E]): Handle[Validated[E, *], E] =
    new Handle[Validated[E, *], E] {
      val applicative: Applicative[Validated[E, *]] =
        Validated.catsDataApplicativeErrorForValidated[E]

      def raise[E2 <: E, A](e: E2): Validated[E, A] = Validated.Invalid(e)

      def handleWith[A](fa: Validated[E, A])(f: E => Validated[E, A]): Validated[E, A] =
        fa match {
          case Validated.Invalid(e) => f(e)
          case v @ Validated.Valid(_) => v
        }
    }

  implicit final def handleIorT[F[_], E](
      implicit E: Semigroup[E],
      F: Monad[F]): Handle[IorT[F, E, *], E] =
    new Handle[IorT[F, E, *], E] {
      val applicative: Applicative[IorT[F, E, *]] = IorT.catsDataMonadErrorForIorT[F, E]

      def raise[E2 <: E, A](e: E2): IorT[F, E, A] = IorT.leftT(e)

      def handleWith[A](fa: IorT[F, E, A])(f: E => IorT[F, E, A]): IorT[F, E, A] =
        IorT(F.flatMap(fa.value) {
          case Ior.Left(e) => f(e).value
          case e @ _ => F.pure(e)
        })
    }

  implicit final def handleIor[E](implicit E: Semigroup[E]): Handle[Ior[E, *], E] =
    new Handle[Ior[E, *], E] {
      val applicative: Applicative[Ior[E, *]] = Ior.catsDataMonadErrorForIor[E]

      def raise[E2 <: E, A](e: E2): Ior[E, A] = Ior.Left(e)

      def handleWith[A](fa: Ior[E, A])(f: E => Ior[E, A]): Ior[E, A] =
        fa match {
          case Ior.Left(e) => f(e)
          case _ => fa
        }
    }

  implicit final def handleKleisli[F[_], E, R](
      implicit F0: Handle[F, E],
      M: Monad[F]): Handle[Kleisli[F, R, *], E] =
    new RaiseMonadPartialOrder[F, Kleisli[F, R, *], E] with Handle[Kleisli[F, R, *], E] {
      val applicative: Applicative[Kleisli[F, R, *]] = Kleisli.catsDataMonadForKleisli[F, R]

      val F: Raise[F, E] = F0
      val lift: MonadPartialOrder[F, Kleisli[F, R, *]] =
        MonadPartialOrder.monadPartialOrderForKleisli[F, R]

      def handleWith[A](fa: Kleisli[F, R, A])(f: E => Kleisli[F, R, A]): Kleisli[F, R, A] =
        Kleisli(r => F0.handleWith(fa.run(r))(e => f(e).run(r)))
    }

  implicit final def handleWriterT[F[_], E, L](
      implicit F0: Handle[F, E],
      M: Monad[F],
      L: Monoid[L]): Handle[WriterT[F, L, *], E] =
    new RaiseMonadPartialOrder[F, WriterT[F, L, *], E] with Handle[WriterT[F, L, *], E] {
      val applicative: Applicative[WriterT[F, L, *]] =
        WriterT.catsDataApplicativeForWriterT[F, L]

      val F: Raise[F, E] = F0
      val lift: MonadPartialOrder[F, WriterT[F, L, *]] =
        MonadPartialOrder.monadPartialOrderForWriterT[F, L]

      def handleWith[A](fa: WriterT[F, L, A])(f: E => WriterT[F, L, A]): WriterT[F, L, A] =
        WriterT(F0.handleWith(fa.run)(e => f(e).run))
    }

  implicit final def handleStateT[F[_], E, S](
      implicit F0: Handle[F, E],
      M: Monad[F]): Handle[StateT[F, S, *], E] =
    new RaiseMonadPartialOrder[F, StateT[F, S, *], E] with Handle[StateT[F, S, *], E] {
      val applicative: Applicative[StateT[F, S, *]] =
        IndexedStateT.catsDataMonadForIndexedStateT[F, S]

      val F: Raise[F, E] = F0
      val lift: MonadPartialOrder[F, StateT[F, S, *]] =
        MonadPartialOrder.monadPartialOrderForStateT[F, S]

      def handleWith[A](fa: StateT[F, S, A])(f: E => StateT[F, S, A]): StateT[F, S, A] =
        StateT(s => F0.handleWith(fa.run(s))(e => f(e).run(s)))
    }

  implicit final def handleRWST[F[_], E, R, L, S](
      implicit F0: Handle[F, E],
      M: Monad[F],
      L: Monoid[L]): Handle[RWST[F, R, L, S, *], E] =
    new RaiseMonadPartialOrder[F, RWST[F, R, L, S, *], E] with Handle[RWST[F, R, L, S, *], E] {
      val applicative: Applicative[RWST[F, R, L, S, *]] =
        IndexedReaderWriterStateT.catsDataMonadForRWST

      val F: Raise[F, E] = F0
      val lift: MonadPartialOrder[F, RWST[F, R, L, S, *]] =
        MonadPartialOrder.monadPartialOrderForRWST

      def handleWith[A](fa: RWST[F, R, L, S, A])(
          f: E => RWST[F, R, L, S, A]): RWST[F, R, L, S, A] =
        RWST { case (r, s) => F0.handleWith(fa.run(r, s))(e => f(e).run(r, s)) }
    }
}

object Handle extends HandleInstances with HandleVariant {

  def apply[F[_], E](implicit ev: Handle[F, E]): Handle[F, E] = ev

  def allowF[F[_], E]: AdHocSyntaxTired[F, E] =
    new AdHocSyntaxTired[F, E](())

  @scala.annotation.nowarn("msg=dubious usage of method hashCode with unit value")
  private[mtl] final class AdHocSyntaxTired[F[_], E](private val unit: Unit) extends AnyVal {
    def apply[A](body: Handle[F, E] => F[A]): Inner[F, E, A] =
      new Inner(body)
  }

  private[mtl] final class Inner[F[_], E, A](private val body: Handle[F, E] => F[A])
      extends AnyVal {
    def rescue(h: E => F[A])(implicit F: ApplicativeThrow[F]): F[A] = {
      val Marker = new AnyRef

      def inner[B](fb: F[B])(f: E => F[B]): F[B] =
        ApplicativeThrow[F].handleErrorWith(fb) {
          case Submarine(e, Marker) => f(e.asInstanceOf[E])
          case t => ApplicativeThrow[F].raiseError(t)
        }

      val fa = body(new Handle[F, E] {
        def applicative = Applicative[F]
        def raise[E2 <: E, B](e: E2): F[B] =
          ApplicativeThrow[F].raiseError(Submarine(e, Marker))
        def handleWith[B](fb: F[B])(f: E => F[B]): F[B] = inner(fb)(f)
      })

      inner(fa)(h)
    }
  }

  private[mtl] final case class Submarine[E](e: E, marker: AnyRef)
      extends RuntimeException
      with NoStackTrace
}
