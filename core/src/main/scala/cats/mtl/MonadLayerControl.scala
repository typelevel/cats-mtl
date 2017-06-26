package cats
package mtl

trait MonadLayerControl[M[_], Inner[_]] extends MonadLayerFunctor[M, Inner] with Serializable {

  type State[A]

  def restore[A](state: State[A]): M[A]

  def layerControl[A](cps: (M ~> (Inner of State)#l) => Inner[A]): M[A]

  def zero[A](state: State[A]): Boolean

}

object MonadLayerControl {
  type Aux[M[_], Inner0[_], State0[_]] = MonadLayerControl[M, Inner0] {
    type State[A] = State0[A]
  }
}

