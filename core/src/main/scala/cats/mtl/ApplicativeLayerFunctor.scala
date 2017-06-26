package cats
package mtl

/**
  * laws:
  * {{{
  * def layerMapRespectsLayer(in: Inner[A])(forward: Inner ~> Inner) = {
  *   layer(forward(in)) <-> layerMap(layer(in))(forward)
  * }
  * }}}
  *
  * internal laws:
  * {{{
  * def layerMapRespectsLayerImapK(ma: M[A])(forward: Inner ~> Inner,
  *                                          backward: Inner ~> Inner) = {
  *   layerIMapK(ma)(forward, backward) <-> layerMap(layer(in))(forward)
  * }
  * }}}
  */
trait ApplicativeLayerFunctor[M[_], Inner[_]] extends ApplicativeLayer[M, Inner] with FunctorLayerFunctor[M, Inner] with Serializable
