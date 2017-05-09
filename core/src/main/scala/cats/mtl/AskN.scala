package cats
package mtl

import cats.data.{ReaderT, StateT}
import cats.mtl.evidence._
import syntax.functor._

trait AskN[F[_], E] {
  implicit val monad: Monad[F]

  type N <: Nat

  def askN: F[E]

  def reader[A](f: E => A): F[A] =
    askN.map(f)
}

object AskN extends LowPri {

  type Aux[N0 <: Nat, F[_], E] = AskN[F, E] {type N = N0}

  implicit def askNInd[N0 <: Nat, T[_[_], _], M[_], E](implicit TM: Monad[CurryT[T, M]#l],
                                                       lift: TransLift.AuxId[T],
                                                       under: AskN.Aux[N0, M, E]
                                                      ): AskN.Aux[Nat.Succ[N0], CurryT[T, M]#l, E] =
    new AskN[CurryT[T, M]#l, E] {
      val monad = TM

      type N = Nat.Succ[N0]

      def askN: T[M, E] =
        lift.liftT(under.askN)
    }

  def ask[F[_], E](implicit askN: AskN[F, E]): F[E] =
    askN.askN

  def askE[E] = new askEPartiallyApplied[E]

  def askF[F[_]] = new askFPartiallyApplied[F]

  def askN[N <: Nat] = new askNPartiallyApplied[N]

  final private[mtl] class askEPartiallyApplied[E](val dummy: Boolean = false) extends AnyVal {
    @inline def apply[F[_]]()(implicit askN: AskN[F, E]): F[E] =
      askN.askN
  }

  final private[mtl] class askFPartiallyApplied[F[_]](val dummy: Boolean = false) extends AnyVal {
    @inline def apply[E]()(implicit askN: AskN[F, E]): F[E] =
      askN.askN
  }

  final private[mtl] class askNPartiallyApplied[N <: Nat](val dummy: Boolean = false) extends AnyVal {
    @inline def apply[E, F[_]]()(implicit askN: AskN.Aux[N, F, E]): F[E] =
      askN.askN
  }

  def reader[F[_], E, A](fun: E => A)(implicit askN: AskN[F, E]): F[A] =
    askN.reader(fun)

  final class ReaderOps[E, A](val fun: E => A) extends AnyVal {
    def reader[F[_]](implicit askN: AskN[F, E]): F[A] =
      askN.reader(fun)
  }


}

trait LowPri {

  implicit def askNReader[M[_], E](implicit M: Monad[M]): AskN.Aux[Nat.Zero, CurryT[ReaderTC[E]#l, M]#l, E] =
    new AskN[CurryT[ReaderTC[E]#l, M]#l, E] {

      val monad =
        ReaderT.catsDataMonadReaderForKleisli

      type N = Nat.Zero

      def askN: ReaderT[M, E, E] =
        ReaderT.ask[M, E]
    }

  object State {
    implicit def askNState[M[_], E](implicit M: Monad[M]): AskN.Aux[Nat.Zero, CurryT[StateTC[E]#l, M]#l, E] =
      new AskN[CurryT[StateTC[E]#l, M]#l, E] {
        val monad =
          StateT.catsDataMonadForStateT(M)

        type N = Nat.Zero

        def askN: StateT[M, E, E] =
          StateT.get[M, E]
      }
  }

}

