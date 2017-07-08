package cats
package mtl

/**
  * `ApplicativeLayerFunctor[M, Inner]` has no additional laws.
  */
trait ApplicativeLayerFunctor[M[_], Inner[_]] extends ApplicativeLayer[M, Inner]
  with FunctorLayerFunctor[M, Inner] with Serializable
