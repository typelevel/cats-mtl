package cats
package mtl

/**
  * `ApplicativeLayer[M, Inner]` has two external laws:
  * {{{
  * def layerRespectsPure[A](a: A) = {
  *   layer(a.pure[Inner]) <-> a.pure[M]
  * }
  * def layerRespectsAp[A, B](m: Inner[A])(f: Inner[A => B]) = {
  *   layer(m).ap(layer(f)) <-> layer(m.ap(f))
  * }
  * }}}
  */
trait ApplicativeLayer[M[_], Inner[_]] extends FunctorLayer[M, Inner] with Serializable {
  val outerInstance: Applicative[M]
  val innerInstance: Applicative[Inner]
}
