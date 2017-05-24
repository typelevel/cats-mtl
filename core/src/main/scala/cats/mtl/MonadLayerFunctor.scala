package cats
package mtl

trait MonadLayerFunctor[M[_], Inner[_]] extends MonadLayer[M, Inner] {
  def layerMap[A](ma: M[A])
                 (trans: Inner ~> Inner): M[A]
}
