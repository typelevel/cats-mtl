package cats
package mtl

/**
  * `ApplicativeLayer[M, Inner]` has the following functionality:
  * - the capability to lift values from the `Applicative` `Inner`
  *   to the `Applicative` `M`, preserving the `Applicative` structure
  * - lifts `Applicative` isomorphisms in `Inner` (`(Inner ~> Inner, Inner ~> Inner)`)
  *   into `Applicative` homomorphisms in `M` (`M ~> M`).
  *   This allows you to "map" a natural transformation over the `Inner` inside `M`,
  *   but only if you can provide an inverse of that natural transformation.
  *
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

object ApplicativeLayer {
  def apply[M[_], Inner[_]](implicit applicativeLayer: ApplicativeLayer[M, Inner]): ApplicativeLayer[M, Inner] = applicativeLayer
}
