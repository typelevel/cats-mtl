package cats
package mtl
package lifting

/**
  * `ApplicativeLayerFunctor` is the capability to lift `Applicative` homomorphisms
  * in `Inner` (`Inner ~> Inner`) into homomorphisms in `M` (`Inner ~> Inner`).
  *
  * `ApplicativeLayerFunctor[M, Inner]` has no additional laws.
  */
trait ApplicativeLayerFunctor[M[_], Inner[_]] extends ApplicativeLayer[M, Inner]
  with FunctorLayerFunctor[M, Inner] with Serializable

object ApplicativeLayerFunctor {
  def apply[M[_], Inner[_]](implicit applicativeLayerFunctor: ApplicativeLayerFunctor[M, Inner]): ApplicativeLayerFunctor[M, Inner] = applicativeLayerFunctor
}
