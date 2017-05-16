package cats
package mtl

trait MonadTransControl[M[_]] extends MonadLayerControl[M] with MonadTransFunctor[M] {
  def transControl[A](cps: MonadTransContinuation[State, Outer, A]): M[A]
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

