package cats
package mtl
package instances

import cats.data.{EitherT, OptionT, Validated}

trait HandleInstances extends HandleLowPriorityInstances1 {
  implicit final def raiseInd[M[_], Inner[_], E](implicit
                                                 lift: MonadLayerControl[M, Inner],
                                                 under: ApplicativeHandle[Inner, E]
                                                ): ApplicativeHandle[M, E] = {
    new DefaultApplicativeHandle[M, E] {
      val applicative = lift.outerInstance
      val raise = instances.raise.raiseInd[M, Inner, E](lift, under.raise)

      def handleWith[A](fa: M[A])(f: E => M[A]): M[A] =
        lift.outerInstance.flatMap(lift.layerControl { nt =>
          under.handleWith(nt(fa))(e => nt(f(e)))
        })(lift.restore)
    }
  }
}

private[instances] trait HandleLowPriorityInstances1 {
  implicit final def handleEitherT[M[_], E](implicit M: Monad[M]): ApplicativeHandle[EitherTC[M, E]#l, E] = {
    new DefaultApplicativeHandle[EitherTC[M, E]#l, E] {
      val applicative: Applicative[EitherTC[M, E]#l] = EitherT.catsDataMonadErrorForEitherT(M)
      val raise: FunctorRaise[EitherTC[M, E]#l, E] = instances.raise.raiseEitherT[M, E]

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
      val raise: FunctorRaise[EitherC[E]#l, E] = instances.raise.raiseEither[E]

      def handleWith[A](fa: Either[E, A])(f: E => Either[E, A]): Either[E, A] = fa match {
        case Left(e) => f(e)
        case r @ Right(_) => r
      }
    }
  }

  implicit final def handleOptionT[M[_]](implicit M: Monad[M]): ApplicativeHandle[OptionTC[M]#l, Unit] = {
    new DefaultApplicativeHandle[OptionTC[M]#l, Unit] {
      val applicative: Applicative[OptionTC[M]#l] = OptionT.catsDataMonadForOptionT(M)
      val raise: FunctorRaise[OptionTC[M]#l, Unit] = instances.raise.raiseOptionT[M]

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
      val raise: FunctorRaise[Option, Unit] = instances.raise.raiseOption

      def handleWith[A](fa: Option[A])(f: Unit => Option[A]): Option[A] = fa match {
        case None => f(())
        case s @ Some(_) => s
      }
    }
  }

  implicit final def handleValidated[E](implicit E: Semigroup[E]): ApplicativeHandle[Validated[E, ?], E] =
    new DefaultApplicativeHandle[Validated[E, ?], E] {
      val applicative: Applicative[Validated[E, ?]] = Validated.catsDataApplicativeErrorForValidated[E]
      val raise: FunctorRaise[Validated[E, ?], E] = instances.raise.raiseValidated[E]

      def handleWith[A](fa: Validated[E, A])(f: E => Validated[E, A]): Validated[E, A] = fa match {
        case Validated.Invalid(e) => f(e)
        case v @ Validated.Valid(_) => v
      }
    }

}

object handle extends HandleInstances
