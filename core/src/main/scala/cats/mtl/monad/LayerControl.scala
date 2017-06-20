package cats
package mtl
package monad

trait LayerControl[M[_], Inner[_]] extends LayerFunctor[M, Inner] {

  type State[A]

  def restore[A](state: State[A]): M[A]

  def layerControl[A](cps: (M ~> (Inner of State)#l) => Inner[A]): M[A]

  def zero[A](state: State[A]): Boolean

}

object LayerControl {
  type Aux[M[_], Inner0[_], State0[_]] = LayerControl[M, Inner0] {
    type State[A] = State0[A]
  }
}

