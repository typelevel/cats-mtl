package cats
package mtl

import cats.data._

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

  implicit final def handleEitherT[M[_], E](implicit M: Monad[M]): ApplicativeHandle[EitherTC[M, E]#l, E] = {
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
      val applicative: Applicative[EitherC[E]#l] = cats.instances.either.catsStdInstancesForEither[E]

      val functor: Functor[EitherC[E]#l] = cats.instances.either.catsStdInstancesForEither

      def raise[A](e: E): Either[E, A] = Left(e)

      def handleWith[A](fa: Either[E, A])(f: E => Either[E, A]): Either[E, A] = fa match {
        case Left(e) => f(e)
        case r @ Right(_) => r
      }
    }
  }

  implicit final def handleOptionT[M[_]](implicit M: Monad[M]): ApplicativeHandle[OptionTC[M]#l, Unit] = {
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

      def handleWith[A](fa: Option[A])(f: Unit => Option[A]): Option[A] = fa match {
        case None => f(())
        case s @ Some(_) => s
      }
    }
  }

  implicit final def handleValidated[E](implicit E: Semigroup[E]): ApplicativeHandle[Validated[E, ?], E] =
    new ApplicativeHandle[Validated[E, ?], E] {
      val applicative: Applicative[Validated[E, ?]] = Validated.catsDataApplicativeErrorForValidated[E]

      val functor: Functor[Validated[E, ?]] = Validated.catsDataApplicativeErrorForValidated[E]

      def raise[A](e: E): Validated[E, A] = Validated.Invalid(e)

      def handleWith[A](fa: Validated[E, A])(f: E => Validated[E, A]): Validated[E, A] = fa match {
        case Validated.Invalid(e) => f(e)
        case v @ Validated.Valid(_) => v
      }
    }
}

object ApplicativeHandle extends ApplicativeHandleInstances {
  def apply[F[_], E](implicit ev: ApplicativeHandle[F, E]): ApplicativeHandle[F, E] = ev
}
