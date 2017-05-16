package cats
package mtl
package instances

// import cats.data.EitherT

trait RaiseInstances extends RaiseLowPriorityInstances {
/*
  implicit def raiseNEither[M[_], E](implicit M: Monad[M]): Raise[CurryT[EitherTC[E]#l, M]#l, E] =
    new Raise[CurryT[EitherTC[E]#l, M]#l, E] {
      val monad = EitherT.catsDataMonadErrorForEitherT(M)

      def raiseError[A](e: E): EitherT[M, E, A] =
        EitherT(M.pure(Left(e)))
    }
 */
}

trait RaiseLowPriorityInstances {
/*
  implicit def raiseNInd[T[_[_], _], M[_], E](implicit TM: Monad[CurryT[T, M]#l],
                                                         lift: TransLift.Aux[T, Applicative],
                                                         under: Raise[M, E]
                                                        ): Raise[CurryT[T, M]#l, E] =
    new Raise[CurryT[T, M]#l, E] {
      val monad = TM

      def raiseError[A](e: E): T[M, A] =
        lift.liftT(under.raiseError[A](e))(under.monad)
    }
 */
}

object raise extends RaiseInstances
