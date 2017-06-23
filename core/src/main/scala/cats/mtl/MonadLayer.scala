package cats
package mtl

/**
  * laws:
  * {{{
  * def mapForwardRespectsLayer(in: Inner[A])(forward: Inner ~> Inner, backward: Inner ~> Inner) = {
  *   layer(forward(in)) <-> imapK(layer(in))(forward, backward)
  * }
  * def layerRespectsPure(a: A) = {
  *   layer(a.pure[Inner]) <-> a.pure[M]
  * }
  * def layerRespectsFlatMap(m: Inner[A])(f: A => Inner[B]) = {
  *   layer(m).flatMap(f andThen layer) <-> layer(m.flatMap(f))
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
trait MonadLayer[M[_], Inner[_]] extends ApplicativeLayer[M, Inner] {
  val outerInstance: Monad[M]
  val innerInstance: Monad[Inner]
}
