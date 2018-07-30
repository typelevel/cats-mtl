package cats
package mtl
package laws

import cats.mtl.lifting._

trait MonadLayerFunctorLaws[M[_], Inner[_]] extends MonadLayerLaws[M, Inner] with ApplicativeLayerFunctorLaws[M, Inner] {
  val monadLayerFunctorInstance: MonadLayerFunctor[M, Inner]
  // no extra laws
}

object MonadLayerFunctorLaws {
  def apply[M[_], Inner[_]](implicit monadLayerFunctor: MonadLayerFunctor[M, Inner]): MonadLayerFunctorLaws[M, Inner] = new MonadLayerFunctorLaws[M, Inner] {
    override lazy val monadLayerFunctorInstance: MonadLayerFunctor[M, Inner] = monadLayerFunctor
    override lazy val monadLayerInstance: MonadLayer[M, Inner] = monadLayerFunctor
    override lazy val applicativeLayerFunctorInstance: ApplicativeLayerFunctor[M, Inner] = monadLayerFunctor
    override lazy val applicativeLayerInstance: ApplicativeLayer[M, Inner] = monadLayerFunctor
    override lazy val functorLayerFunctorInstance: FunctorLayerFunctor[M, Inner] = monadLayerFunctor
    override lazy val functorLayerInstance: FunctorLayer[M, Inner] = monadLayerFunctor
  }
}
