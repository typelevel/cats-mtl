package cats
package mtl
package laws

class FunctorLayerLaws[M[_], Inner[_]](implicit val functorLayerInstance: FunctorLayer[M, Inner]) {
  implicit val functor: Functor[M] = functorLayerInstance.outerInstance
  implicit val functorInner: Functor[Inner] = functorLayerInstance.innerInstance

  import functorLayerInstance._

  def mapForwardRespectsLayer[A](in: Inner[A])(forward: Inner ~> Inner, backward: Inner ~> Inner): IsEq[M[A]] = {
    layer(forward(in)) <-> layerImapK(layer(in))(forward, backward)
  }

}

object FunctorLayerLaws
