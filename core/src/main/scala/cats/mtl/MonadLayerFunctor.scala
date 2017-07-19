package cats
package mtl

/**
  * `MonadLayerFunctor` is the capability to lift `Monad` homomorphisms
  * in `Inner` (`Inner ~> Inner`) into homomorphisms in `M` (`Inner ~> Inner`).
  *
  * `MonadLayerFunctor[M, Inner]` has no additional laws.
  */
trait MonadLayerFunctor[M[_], Inner[_]] extends MonadLayer[M, Inner] with ApplicativeLayerFunctor[M, Inner] with Serializable

object MonadLayerFunctor {
  def apply[M[_], Inner[_]](implicit monadLayerFunctor: MonadLayerFunctor[M, Inner]): MonadLayerFunctor[M, Inner] = monadLayerFunctor
}
