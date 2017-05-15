package cats
package mtl

trait MonadLayerControl[M[_]] extends MonadLayerFunctor[M] {

  type State[A]

  def restore[A](state: State[A]): M[A]

  def layerControl[A](cps: (M ~> (Inner of State)#l) => Inner[A]): M[A]

  def zero[A](state: State[A]): Boolean

}
