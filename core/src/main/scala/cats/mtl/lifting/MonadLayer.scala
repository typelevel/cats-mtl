package cats
package mtl
package lifting

/**
  * `MonadLayer[M, Inner]` has the following functionality:
  * - lifts values from the `Monad` `Inner` to the `Monad` `M`.
  * - lifts `Monad` isomorphisms in `Inner` (`(Inner ~> Inner, Inner ~> Inner)`)
  *   into `Monad` homomorphisms in `M` (`M ~> M`).
  *   This allows you to "map" a natural transformation over the `Inner` inside `M`,
  *   but only if you can provide an inverse of that natural transformation.
  *
  * `MonadLayer` has one external law:
  * {{{
  * def layerRespectsFlatMap[A, B](m: Inner[A])(f: A => Inner[B]) = {
  *   layer(m).flatMap(f andThen layer) <-> layer(m.flatMap(f))
  * }
  * }}}
  */
trait MonadLayer[M[_], Inner[_]] extends ApplicativeLayer[M, Inner] {
  val outerInstance: Monad[M]
  val innerInstance: Monad[Inner]
}

object MonadLayer {
  def apply[M[_], Inner[_]](implicit monadLayer: MonadLayer[M, Inner]): MonadLayer[M, Inner] = monadLayer
}
