package cats
package mtl

/**
  * `MonadLayerFunctor[M, Inner]` has two external laws:
  * {{{
  * def layerMapRespectsLayer(in: Inner[A])(forward: Inner ~> Inner) = {
  *   layer(forward(in)) <-> layerMap(layer(in))(forward, backward)
  * }
  * def layerMapRespectsLayerImapK(ma: M[A])(forward: Inner ~> Inner,
  *                                          backward: Inner ~> Inner) = {
  *   layerIMapK(ma)(forward, backward) <-> layerMap(layer(in))(forward)
  * }
  * }}}
  */
trait MonadLayerFunctor[M[_], Inner[_]] extends MonadLayer[M, Inner] with ApplicativeLayerFunctor[M, Inner] with Serializable

object MonadLayerFunctor {
  def apply[M[_], Inner[_]](implicit monadLayerFunctor: MonadLayerFunctor[M, Inner]): MonadLayerFunctor[M, Inner] = monadLayerFunctor
}
