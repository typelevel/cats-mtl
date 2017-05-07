package cats.mtl.evidence

import cats.{Monad, TransLift}
import cats.mtl.evidence.MapCanDo.CurryT

trait FindTrue[L <: BoolList] {
  type Out <: Nat
}

object FindTrue {
  implicit def findTrueTCons[Tail <: BoolList]: FindTrue[BoolList.Cons[Bool.True, Tail]] = {
    new FindTrue[BoolList.Cons[Bool.True, Tail]] {
      override type Out = Nat.Zero
    }
  }

  implicit def findTrueFCons[Tail <: BoolList](implicit under: FindTrue[Tail]): FindTrue[BoolList.Cons[Bool.False, Tail]] = {
    new FindTrue[BoolList.Cons[Bool.False, Tail]] {
      override type Out = Nat.Succ[under.Out]
    }
  }
}

trait CanDo[M[_], Eff[_]] {
  type Out <: Bool
}

trait MapCanDo[Eff[_], M[_]] {
  type Out <: BoolList
}

object MapCanDo {
  implicit def mapCanDoBase[Eff[_], M[_]](implicit ev: CanDo[M, Eff]): MapCanDo[Eff, M] = {
    new MapCanDo[Eff, M] {
      type Out = BoolList.Cons[ev.Out, BoolList.Nil]
    }
  }

  type CurryT[T[_[_], _], M] = {type l[A] = T[M, A]}

  implicit def mapCanDoInd[Eff[_], T[_[_], _], M[_]](implicit ev: CanDo[CurryT[T, M]#l, Eff],
                                                     under: MapCanDo[Eff, M]): MapCanDo[Eff, CurryT[T, M]#l] = {
    new MapCanDo[Eff, CurryT[T, M]#l] {
      type Out = BoolList.Cons[ev.Out, under.Out]
    }
  }
}

object CanDo {
  type Find[Eff[_], M[_]] = FindTrue[MapCanDo[Eff, M]]
}

trait MonadLiftN[N <: Nat, M[_]] {
  type Down[A]

  def liftN[A](down: Down[A]): M[A]
}

object MonadLiftN {
  implicit def zMonadLiftNBase[M[_]]: MonadLiftN[Nat.Zero, M] = {
    new MonadLiftN[Nat.Zero, M] {
      override type Down[A] = M[A]

      def liftN[A](down: M[A]): M[A] = {
        down
      }
    }
  }

  implicit def zMonadLiftNInd[N <: Nat, T[_[_], _], M[_]](
                                                        implicit under: MonadLiftN[N, M],
                                                        trans: TransLift[T]
                                                      ): MonadLiftN[Nat.Succ[N], CurryT[T, M]#l] = {
    new MonadLiftN[Nat.Succ[N], CurryT[T, M]#l] {
      override type Down[A] = under.Down[A]

      def liftN[A](down: under.Down[A]): T[M, A] = {
        trans.liftT(under.liftN(down))
      }
    }
  }

}

// this really, really needs kind-poly.
trait EffWriter[W]

trait EffListen[W]

trait EffReader[E]

trait EffLocal[E]

trait EffState[S]

trait EffRaise[E]

trait EffHandle[E]
