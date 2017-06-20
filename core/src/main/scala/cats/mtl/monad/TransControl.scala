package cats
package mtl
package monad

trait TransControl[M[_], Inner[_]] extends LayerControl[M, Inner] with TransFunctor[M, Inner] {
  def transControl[A](cps: MonadTransContinuation[State, Outer, A]): M[A]
}

object TransControl {
  type AuxS[M[_], State0[_], Inner0[_]] = TransControl[M, Inner0] {
    type State[A] = State0[A]
  }
  type AuxO[M[_], Inner0[_], Outer0[_[_], _]] = TransControl[M, Inner0] {
    type Outer[F[_], A] = Outer0[F, A]
  }
  type Aux[M[_], State0[_], Inner0[_], Outer0[_[_], _]] = TransControl[M, Inner0] {
    type State[A] = State0[A]
    type Outer[F[_], A] = Outer0[F, A]
  }
}

trait MonadTransContinuation[State[_], Outer[_[_], _], A] {
  def apply[N[_], NInner[_]](cps: (N ~> (NInner of State)#l))
                            (implicit mt: TransControl.AuxO[N, NInner, Outer]): NInner[A]
}

object MonadTransContinuation {
  def fixTypeParams[M[_], Inner[_], Outer[_[_], _], State[_], A]
  (cps: MonadTransContinuation[State, Outer, A])
  (implicit mt: TransControl.Aux[M, State, Inner, Outer]): (M ~> (Inner of State)#l) => Inner[A] = {
    cps(_)
  }

}
