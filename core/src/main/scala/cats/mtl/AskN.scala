package cats
package mtl

import cats.data.{ReaderT, StateT}
import cats.mtl.evidence._
import syntax.functor._

trait AskN[N <: Nat, F[_], E] {
  implicit val monad: Monad[F]

  def askN: F[E]

  def reader[A](f: E => A)(implicit F: Functor[F]): F[A] =
    askN.map(f)
}

object AskN {

  implicit def askNReader[M[_], E](implicit M: Monad[M]): AskN[Nat.Zero, CurryT[ReaderTC[E]#l, M]#l, E] =
    new AskN[Nat.Zero, CurryT[ReaderTC[E]#l, M]#l, E] {
      val monad =
        ReaderT.catsDataMonadReaderForKleisli(M)

      def askN: ReaderT[M, E, E] =
        ReaderT.ask[M, E]
    }

  object State {
    implicit def askNState[M[_], E](implicit M: Monad[M]): AskN[Nat.Zero, CurryT[StateTC[E]#l, M]#l, E] =
      new AskN[Nat.Zero, CurryT[StateTC[E]#l, M]#l, E] {
        val monad =
          StateT.catsDataMonadForStateT(M)

        def askN: StateT[M, E, E] =
          StateT.get[M, E]
      }
  }

  implicit def askNInd[N <: Nat, T[_[_], _], M[_], E](implicit TM: Monad[CurryT[T, M]#l],
                                                      lift: TransLift.AuxId[T],
                                                      under: AskN[N, M, E]
                                                     ): AskN[Nat.Succ[N], CurryT[T, M]#l, E] =
    new AskN[Nat.Succ[N], CurryT[T, M]#l, E] {
      val monad = TM

      def askN: T[M, E] =
        lift.liftT(under.askN)
    }


}

object AskMaterializer {
  def askSummon[N <: Nat, F[_], E](implicit find: Find.Aux[EffAsk[E], F, N],
                                           askN: AskN[N, F, E]): Ask[E, F] =
    askN.asInstanceOf[Ask[E, F]]
}
