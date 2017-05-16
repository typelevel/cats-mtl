package cats
package mtl

trait MonadLayerControl[M[_]] extends MonadLayerFunctor[M] {

  type State[A]

  def restore[A](state: State[A]): M[A]

  def layerControl[A](cps: (M ~> (Inner of State)#l) => Inner[A]): M[A]

  def zero[A](state: State[A]): Boolean

}

object MonadLayerControl {
  type Aux[M[_], Inner0[_], State0[_]] = MonadLayerControl[M] {
    type Inner[A] = Inner0[A]
    type State[A] = State0[A]
  }
}

