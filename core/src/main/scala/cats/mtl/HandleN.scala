package cats
package mtl

import cats.data.EitherT
import evidence.Nat

// this may be wrong.
trait HandleN[N <: Nat, F[_], E] {
  val raise: RaiseN[N, F, E]

  type Down[A]

  def materialize[A](fa: F[A]): F[Down[E Either A]]
}

object HandleN {
  implicit def handleNEither[M[_], E](implicit M: Monad[M]): HandleN[Nat.Zero, CurryT[EitherTC[E]#l, M]#l, E] =
    new HandleN[Nat.Zero, CurryT[EitherTC[E]#l, M]#l, E] {
      val monad = M

      type Down[A] = M[A]

      val raise: RaiseN[Nat.Zero, CurryT[EitherTC[E]#l, M]#l, E] =
        RaiseN.raiseNEither(M)

      def materialize[A](fa: EitherT[M, E, A]): EitherT[M, E, M[Either[E, A]]] =
        EitherT.pure(fa.value)
    }

  implicit def handleNInd[N <: Nat, T[_[_], _], M[_], E](implicit TM: Monad[CurryT[T, M]#l],
                                                         lift: TransLift.AuxId[T],
                                                         mf: MMonad[T],
                                                         under: HandleN[N, M, E]
                                                        ): HandleN[Nat.Succ[N], CurryT[T, M]#l, E] =
    new HandleN[Nat.Succ[N], CurryT[T, M]#l, E] {
      val monad = TM

      val raise: RaiseN[Nat.Succ[N], CurryT[T, M]#l, E] =
        RaiseN.raiseNInd[N, T, M, E](TM, lift, under.raise)

      type Down[A] = under.Down[A]

      def materialize[A](fa: T[M, A]): T[M, under.Down[Either[E, A]]] =
        mf.low(fa)(a => lift.liftT(under.materialize(a)))
    }

}
