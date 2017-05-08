package cats
package mtl
package evidence

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

trait CanDo[M[_], Eff] {
  type Out <: Bool
}

trait MapCanDo[Eff, M[_]] {
  type Out <: BoolList
}

object MapCanDo {
  implicit def mapCanDoBase[Eff, M[_]](implicit ev: CanDo[M, Eff]): MapCanDo[Eff, M] =
    new MapCanDo[Eff, M] {
      type Out = BoolList.Cons[ev.Out, BoolList.Nil]
    }

  implicit def mapCanDoInd[Eff, T[_[_], _], M[_]](implicit ev: CanDo[CurryT[T, M]#l, Eff],
                                                  under: MapCanDo[Eff, M]): MapCanDo[Eff, CurryT[T, M]#l] =
    new MapCanDo[Eff, CurryT[T, M]#l] {
      type Out = BoolList.Cons[ev.Out, under.Out]
    }
}

trait MonadLiftN[N <: Nat, M[_]] {
  type Down[A]

  def liftN[A](down: Down[A]): M[A]
}

object MonadLiftN {
  implicit def zMonadLiftNBase[M[_]]: MonadLiftN[Nat.Zero, M] =
    new MonadLiftN[Nat.Zero, M] {
      override type Down[A] = M[A]

      def liftN[A](down: M[A]): M[A] = down
    }

  implicit def zMonadLiftNInd[N <: Nat, T[_[_], _], M[_]](
                                                           implicit under: MonadLiftN[N, M],
                                                           lift: TransLift.AuxId[T]
                                                         ): MonadLiftN[Nat.Succ[N], CurryT[T, M]#l] =
    new MonadLiftN[Nat.Succ[N], CurryT[T, M]#l] {
      override type Down[A] = under.Down[A]

      def liftN[A](down: under.Down[A]): T[M, A] =
        lift.liftT(under.liftN(down))
    }

}

trait EffTell[W]

trait EffListen[W]

trait EffAsk[E]

trait EffLocal[E]

trait EffState[S]

trait EffRaise[E]

trait EffHandle[E]
