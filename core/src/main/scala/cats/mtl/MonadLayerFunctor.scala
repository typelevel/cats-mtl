package cats
package mtl

trait MonadLayerFunctor[M[_]] extends MonadLayer[M] {
  def layerMap[A](ma: M[A])
                 (trans: Inner ~> Inner): M[A]
}
