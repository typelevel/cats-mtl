package cats
package mtl
package monad

/**
  * laws:
  * {{{
  * def layerMapRespectsLayer(in: Inner[A])(forward: Inner ~> Inner) = {
  *   layer(forward(in)) <-> layerMap(layer(in))(forward, backward)
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
trait LayerFunctor[M[_], Inner[_]] extends Layer[M, Inner] with applicative.LayerFunctor[M, Inner]
