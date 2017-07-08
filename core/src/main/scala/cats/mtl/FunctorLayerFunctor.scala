package cats
package mtl

/**
  * `FunctorLayerFunctor[M, Inner]` has two external laws:
  * {{{
  *
  * def layerMapRespectsLayerImapK[A](ma: M[A])(forward: Inner ~> Inner,
  *                                          backward: Inner ~> Inner) = {
  *   layerImapK(ma)(forward, backward) <-> layerMapK(ma)(forward)
  * }
  * }}}
  *
  * `FunctorLayerFunctor[M, Inner]` has one free law, that is,
  * one law guaranteed by other laws and parametricity:
  * {{{
  * def layerMapRespectsLayer[A](in: Inner[A])(forward: Inner ~> Inner) = {
  *   layer(forward(in)) <-> layerMapK(layer(in))(forward)
  * }
  * }}}
  */
trait FunctorLayerFunctor[M[_], Inner[_]] extends FunctorLayer[M, Inner] with Serializable {
  def layerMapK[A](ma: M[A])
                  (trans: Inner ~> Inner): M[A]

  def layerImapK[A](ma: M[A])(forward: Inner ~> Inner, backward: Inner ~> Inner): M[A] =
    layerMapK(ma)(forward)
}

object FunctorLayerFunctor {
  def apply[M[_], Inner[_]](implicit functorLayerFunctor: FunctorLayerFunctor[M, Inner]): FunctorLayerFunctor[M, Inner] = functorLayerFunctor
}
