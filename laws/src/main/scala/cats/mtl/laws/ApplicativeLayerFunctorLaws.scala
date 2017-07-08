package cats
package mtl
package laws

trait ApplicativeLayerFunctorLaws[M[_], Inner[_]] extends ApplicativeLayerLaws[M, Inner] with FunctorLayerFunctorLaws[M, Inner] {
  implicit val applicativeLayerFunctorInstance: ApplicativeLayerFunctor[M, Inner]
}

object ApplicativeLayerFunctorLaws {
  def apply[M[_], Inner[_]](implicit instance0: ApplicativeLayerFunctor[M, Inner]): ApplicativeLayerFunctorLaws[M, Inner] = {
    new ApplicativeLayerFunctorLaws[M, Inner] {
      override implicit val applicativeLayerFunctorInstance: ApplicativeLayerFunctor[M, Inner] = instance0
      override implicit val functorLayerFunctorInstance: FunctorLayerFunctor[M, Inner] = instance0
      override implicit val applicativeLayerInstance: ApplicativeLayer[M, Inner] = instance0
      override implicit val functorLayerInstance: FunctorLayer[M, Inner] = instance0
    }
  }
}
