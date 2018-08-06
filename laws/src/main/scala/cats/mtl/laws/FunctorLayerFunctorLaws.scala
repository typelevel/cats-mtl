package cats
package mtl
package laws

import cats.laws.IsEq
import cats.laws.IsEqArrow
import cats.mtl.lifting.{FunctorLayer, FunctorLayerFunctor}

trait FunctorLayerFunctorLaws[M[_], Inner[_]] extends FunctorLayerLaws[M, Inner] {
  val functorLayerFunctorInstance: FunctorLayerFunctor[M, Inner]
  import functorLayerFunctorInstance._

  // external law:
  def layerMapRespectsLayerImapK[A](ma: M[A])(forward: Inner ~> Inner,
                                              backward: Inner ~> Inner): IsEq[M[A]] = {
    layerImapK(ma)(forward, backward) <-> layerMapK(ma)(forward)
  }
}

object FunctorLayerFunctorLaws {
  def apply[M[_], Inner[_]](implicit instance0: FunctorLayerFunctor[M, Inner]): FunctorLayerFunctorLaws[M, Inner] = {
    new FunctorLayerFunctorLaws[M, Inner] {
      lazy val functorLayerFunctorInstance: FunctorLayerFunctor[M, Inner] = instance0
      override lazy val functorLayerInstance: FunctorLayer[M, Inner] = instance0
    }
  }
}
