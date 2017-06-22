package cats
package mtl
package applicative

/**
  * laws:
  * {{{
  * def mapForwardRespectsLayer(in: Inner[A])(forward: Inner ~> Inner, backward: Inner ~> Inner) = {
  *   layer(forward(in)) <-> imapK(layer(in))(forward, backward)
  * }
  * def layerRespectsPure(a: A) = {
  *   layer(a.pure[Inner]) <-> a.pure[M]
  * }
  * def layerRespectsAp(m: Inner[A])(f: Inner[A => B]) = {
  *   layer(m).ap(layer(f)) <-> layer(m.ap(f))
  * }
  * def mapIso(ma: M[A])(forward: Inner ~> Inner, backward: Inner ~> Inner) = {
  *   if (forward andThen backward == FunctionK.id[Inner]) {
  *     imapK(ma)(forward, backward) <-> ma
  *   } else {
  *     true
  *   }
  * }
  * }}}
  */
trait Layer[M[_], Inner[_]] extends functor.Layer[M, Inner] {
  val outerInstance: Applicative[M]
  val innerInstance: Applicative[Inner]
}
