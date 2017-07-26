package cats
package mtl
package laws

trait ApplicativeLayerFunctorLaws[M[_], Inner[_]] extends ApplicativeLayerLaws[M, Inner] with FunctorLayerFunctorLaws[M, Inner] {
  implicit val applicativeLayerFunctorInstance: ApplicativeLayerFunctor[M, Inner]
  // no extra laws
}

object ApplicativeLayerFunctorLaws {
  def apply[M[_], Inner[_]](implicit instance0: ApplicativeLayerFunctor[M, Inner]): ApplicativeLayerFunctorLaws[M, Inner] = {
    new ApplicativeLayerFunctorLaws[M, Inner] {
      override lazy val applicativeLayerFunctorInstance: ApplicativeLayerFunctor[M, Inner] = instance0
      override lazy val functorLayerFunctorInstance: FunctorLayerFunctor[M, Inner] = instance0
      override lazy val applicativeLayerInstance: ApplicativeLayer[M, Inner] = instance0
      override lazy val functorLayerInstance: FunctorLayer[M, Inner] = instance0
    }
  }
}
