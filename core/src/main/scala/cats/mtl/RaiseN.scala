package cats
package mtl

import cats.data.EitherT
import evidence._

trait RaiseN[N <: Nat, F[_], E] {
  val monad: Monad[F]

  def raiseError[A](e: E): F[A]
}

object RaiseN {

  implicit def raiseNEither[M[_], E](implicit M: Monad[M]): RaiseN[Nat.Zero, CurryT[EitherTC[E]#l, M]#l, E] =
    new RaiseN[Nat.Zero, CurryT[EitherTC[E]#l, M]#l, E] {
      val monad = EitherT.catsDataMonadErrorForEitherT(M)

      def raiseError[A](e: E): EitherT[M, E, A] =
        EitherT(M.pure(Left(e)))
    }

  implicit def raiseNInd[N <: Nat, T[_[_], _], M[_], E](implicit TM: Monad[CurryT[T, M]#l],
                                                        lift: TransLift.AuxId[T],
                                                        under: RaiseN[N, M, E]
                                                       ): RaiseN[Nat.Succ[N], CurryT[T, M]#l, E] =
    new RaiseN[Nat.Succ[N], CurryT[T, M]#l, E] {
      val monad = TM

      def raiseError[A](e: E): T[M, A] =
        lift.liftT(under.raiseError(e))
    }


}
