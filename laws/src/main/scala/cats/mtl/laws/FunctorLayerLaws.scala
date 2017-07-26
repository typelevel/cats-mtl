package cats
package mtl
package laws

trait FunctorLayerLaws[M[_], Inner[_]] {
  implicit val functorLayerInstance: FunctorLayer[M, Inner]

  import functorLayerInstance._

  // external law:
  def mapForwardRespectsLayer[A](in: Inner[A])(forward: Inner ~> Inner, backward: Inner ~> Inner): IsEq[M[A]] = {
    layer(forward(in)) <-> layerImapK(layer(in))(forward, backward)
  }

}

object FunctorLayerLaws {
  def apply[M[_], Inner[_]](implicit instance0: FunctorLayer[M, Inner]): FunctorLayerLaws[M, Inner] = {
    new FunctorLayerLaws[M, Inner] {
      lazy val functorLayerInstance: FunctorLayer[M, Inner] = instance0
    }
  }
}
