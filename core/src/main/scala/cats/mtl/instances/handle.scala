package cats
package mtl
package instances

import cats.data.{EitherT, OptionT, Validated}

trait HandleInstances extends HandleLowPriorityInstances1

private[instances] trait HandleLowPriorityInstances1 {
  implicit final def handleEitherT[M[_], E](implicit M: Monad[M]): ApplicativeHandle[EitherTC[M, E]#l, E] = {
    new DefaultApplicativeHandle[EitherTC[M, E]#l, E] {
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
    new DefaultApplicativeHandle[EitherC[E]#l, E] {
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
    new DefaultApplicativeHandle[OptionTC[M]#l, Unit] {
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
    new DefaultApplicativeHandle[Option, Unit] {
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
    new DefaultApplicativeHandle[Validated[E, ?], E] {
      val applicative: Applicative[Validated[E, ?]] = Validated.catsDataApplicativeErrorForValidated[E]

      val functor: Functor[Validated[E, ?]] = Validated.catsDataApplicativeErrorForValidated[E]

      def raise[A](e: E): Validated[E, A] = Validated.Invalid(e)

      def handleWith[A](fa: Validated[E, A])(f: E => Validated[E, A]): Validated[E, A] = fa match {
        case Validated.Invalid(e) => f(e)
        case v @ Validated.Valid(_) => v
      }
    }

}

object handle extends HandleInstances
