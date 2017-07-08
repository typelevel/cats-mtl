package cats
package mtl

/**
  * `ApplicativeLayerFunctor[M, Inner]` has no additional laws.
  */
trait ApplicativeLayerFunctor[M[_], Inner[_]] extends ApplicativeLayer[M, Inner]
  with FunctorLayerFunctor[M, Inner] with Serializable

object ApplicativeLayerFunctor {
  def apply[M[_], Inner[_]](implicit applicativeLayerFunctor: ApplicativeLayerFunctor[M, Inner]): ApplicativeLayerFunctor[M, Inner] = applicativeLayerFunctor
}
