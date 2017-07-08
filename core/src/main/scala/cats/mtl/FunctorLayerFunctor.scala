package cats
package mtl

/**
  * `FunctorLayerFunctor[M, Inner]` has two external laws:
  * {{{
  * def layerMapRespectsLayer(in: Inner[A])(forward: Inner ~> Inner) = {
  *   layer(forward(in)) <-> layerMap(layer(in))(forward)
  * }
  * def layerMapRespectsLayerImapK(ma: M[A])(forward: Inner ~> Inner,
  *                                          backward: Inner ~> Inner) = {
  *   layerIMapK(ma)(forward, backward) <-> layerMap(ma)(forward)
  * }
  * }}}
  */
trait FunctorLayerFunctor[M[_], Inner[_]] extends FunctorLayer[M, Inner] with Serializable {
  def layerMapK[A](ma: M[A])
                  (trans: Inner ~> Inner): M[A]

  def layerImapK[A](ma: M[A])(forward: Inner ~> Inner, backward: Inner ~> Inner): M[A] =
    layerMapK(ma)(forward)
}
