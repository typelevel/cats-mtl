package cats
package mtl
package evidence

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

  implicit def zMonadLiftNInd[N <: Nat, T[_[_], _], M[_]](implicit under: MonadLiftN[N, M],
                                                          lift: TransLift.AuxId[T]
                                                         ): MonadLiftN[Nat.Succ[N], CurryT[T, M]#l] =
    new MonadLiftN[Nat.Succ[N], CurryT[T, M]#l] {
      override type Down[A] = under.Down[A]

      def liftN[A](down: under.Down[A]): T[M, A] =
        lift.liftT(under.liftN(down))
    }

}

