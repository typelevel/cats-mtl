package cats
package mtl

trait MonadTransControl[M[_], Inner[_]] extends MonadLayerControl[M, Inner] with MonadTransFunctor[M, Inner] {
  def transControl[A](cps: MonadTransContinuation[State, Outer, A]): M[A]
}

object MonadTransControl {
  type Aux[M[_], State0[_], Inner0[_], Outer0[_[_], _]] = MonadTransControl[M, Inner0] {
    type State[A] = State0[A]
    type Outer[F[_], A] = Outer0[F, A]
  }
}

trait MonadTransContinuation[State[_], Outer[_[_], _], A] {
  def apply[N[_], NInner[_]](cps: (N ~> (NInner of State)#l))
                            (implicit mt: MonadTrans.AuxIO[N, NInner, Outer]): NInner[A]
}

object MonadTransContinuation {
  def fixTypeParams[M[_], Inner[_], Outer[_[_], _], State[_], A]
  (cps: MonadTransContinuation[State, Outer, A])
  (implicit mt: MonadLayerControl.Aux[M, Inner, State] with
    MonadTrans.AuxIO[M, Inner, Outer]): (M ~> (Inner of State)#l) => Inner[A] = {
    cps(_)
  }

}

