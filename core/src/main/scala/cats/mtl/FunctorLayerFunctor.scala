package cats
package mtl

/**
  * `FunctorLayerFunctor` is the capability to lift homomorphisms in `Inner` (`Inner ~> Inner`) into
  * homomorphisms in `M` (`Inner ~> Inner`).
  *
  * `FunctorLayerFunctor[M, Inner]` has two external laws:
  * {{{
  * def layerMapRespectsLayerImapK[A](ma: M[A])(forward: Inner ~> Inner,
  *                                          backward: Inner ~> Inner) = {
  *   layerImapK(ma)(forward, backward) <-> layerMapK(ma)(forward)
  * }
  *
  * def layerMapRespectsLayer[A](in: Inner[A])(forward: Inner ~> Inner) = {
  *   layer(forward(in)) <-> layerMapK(layer(in))(forward)
  * }
  *
  * }}}
  *
  * `FunctorLayerFunctor[M, Inner]` has one free law, that is,
  * one law guaranteed by other laws and parametricity:
  * {{{
  * def layerMapRespectsId[A](in: M[A]) = {
  *   in <-> layerImapK(in)(FunctionK.id, FunctionK.id)
  * }
  * Justification:
  * layerImapK(in)(FunctionK.id, FunctionK.id) <-> in [by layerMapRespectsLayerImapK[A]]
  * layerMapK(in)(FunctionK.id) <-> layerImapK(FunctionK.id, FunctionK.id) [by layerImapRespectsId[A]]
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
