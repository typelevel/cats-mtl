package cats
package mtl
package instances

import cats.data.{ReaderT, StateT}
import cats.mtl.evidence.Nat

trait AskInstances extends AskInstancesLowPriority {

  implicit def askInd[N0 <: Nat, T[_[_], _], M[_], E](implicit TM: Monad[CurryT[T, M]#l],
                                                      lift: TransLift.AuxId[T],
                                                      under: Ask.Aux[N0, M, E]
                                                      ): Ask.Aux[Nat.Succ[N0], CurryT[T, M]#l, E] =
    new Ask[CurryT[T, M]#l, E] {
      val monad = TM

      type N = Nat.Succ[N0]

      def askN: T[M, E] =
        lift.liftT(under.askN)
    }

}

trait AskInstancesLowPriority {

  implicit def askReader[M[_], E](implicit M: Monad[M]): Ask.Aux[Nat.Zero, CurryT[ReaderTC[E]#l, M]#l, E] =
    new Ask[CurryT[ReaderTC[E]#l, M]#l, E] {

      val monad =
        ReaderT.catsDataMonadReaderForKleisli

      type N = Nat.Zero

      def askN: ReaderT[M, E, E] =
        ReaderT.ask[M, E]
    }

  implicit def askNState[M[_], E](implicit M: Monad[M]): Ask.Aux[Nat.Zero, CurryT[StateTC[E]#l, M]#l, E] =
    new Ask[CurryT[StateTC[E]#l, M]#l, E] {
      val monad =
        StateT.catsDataMonadForStateT(M)

      type N = Nat.Zero

      def askN: StateT[M, E, E] =
        StateT.get[M, E]
    }

}

object ask extends AskInstances

