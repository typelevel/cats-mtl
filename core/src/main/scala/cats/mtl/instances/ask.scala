package cats
package mtl
package instances

import cats.data.{ReaderT, StateT}
import cats.mtl.evidence.Nat

trait AskInstances extends AskInstancesLowPriority {

  implicit def askInd[M[_], Inner[_], E](implicit
                                         lift: MonadTrans.AuxI[M, Inner],
                                         under: Ask[Inner, E]
                                        ): Ask[M, E] =
    new Ask[M, E] {
      val monad = lift.monad

      def ask: M[E] =
        lift.layer(under.ask)
    }

}

trait AskInstancesLowPriority {

  implicit def askReader[M[_], E](implicit M: Monad[M]): Ask[CurryT[ReaderTCE[E]#l, M]#l, E] =
    new Ask[CurryT[ReaderTCE[E]#l, M]#l, E] {

      val monad =
        ReaderT.catsDataMonadReaderForKleisli

      def ask: ReaderT[M, E, E] =
        ReaderT.ask[M, E]
    }

  implicit def askNState[M[_], E](implicit M: Monad[M]): Ask[CurryT[StateTC[E]#l, M]#l, E] =
    new Ask[CurryT[StateTC[E]#l, M]#l, E] {
      val monad =
        StateT.catsDataMonadForStateT(M)

      def ask: StateT[M, E, E] =
        StateT.get[M, E]
    }

}

object ask extends AskInstances

