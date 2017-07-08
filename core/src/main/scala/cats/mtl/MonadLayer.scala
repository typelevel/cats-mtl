package cats
package mtl

/**
  * `MonadLayer` has two external laws:
  * {{{
  * def layerRespectsPure(a: A) = {
  *   layer(a.pure[Inner]) <-> a.pure[M]
  * }
  * def layerRespectsFlatMap(m: Inner[A])(f: A => Inner[B]) = {
  *   layer(m).flatMap(f andThen layer) <-> layer(m.flatMap(f))
  * }
  * }}}
  */
trait MonadLayer[M[_], Inner[_]] extends ApplicativeLayer[M, Inner] with Serializable {
  val outerInstance: Monad[M]
  val innerInstance: Monad[Inner]
}

object MonadLayer {
  def apply[M[_], Inner[_]](implicit monadLayer: MonadLayer[M, Inner]): MonadLayer[M, Inner] = monadLayer
}
