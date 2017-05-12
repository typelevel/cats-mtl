package cats
package mtl
package instances

import cats.data.EitherT
import evidence._

trait RaiseInstances extends RaiseLowPriorityInstances {

  implicit def raiseNEither[M[_], E](implicit M: Monad[M]): RaiseN.Aux[Nat.Zero, CurryT[EitherTC[E]#l, M]#l, E] =
    new Raise[CurryT[EitherTC[E]#l, M]#l, E] {
      val monad = EitherT.catsDataMonadErrorForEitherT(M)

      type N = Nat.Zero

      def raiseError[A](e: E): EitherT[M, E, A] =
        EitherT(M.pure(Left(e)))
    }

}

trait RaiseLowPriorityInstances {

  implicit def raiseNInd[N0 <: Nat, T[_[_], _], M[_], E](implicit TM: Monad[CurryT[T, M]#l],
                                                         lift: TransLift.Aux[T, Applicative],
                                                         under: RaiseN.Aux[N0, M, E]
                                                        ): RaiseN.Aux[Nat.Succ[N0], CurryT[T, M]#l, E] =
    new Raise[CurryT[T, M]#l, E] {
      val monad = TM

      type N = Nat.Succ[N0]

      def raiseError[A](e: E): T[M, A] =
        lift.liftT(under.raiseError[A](e))(under.monad)
    }

}

object raise extends RaiseInstances