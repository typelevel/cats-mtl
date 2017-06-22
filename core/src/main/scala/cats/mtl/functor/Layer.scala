package cats
package mtl
package functor

/**
  * `functor.Layer` has external laws:
  * {{{
  * def mapForwardRespectsLayer(in: Inner[A])(forward: Inner ~> Inner, backward: Inner ~> Inner) = {
  *   layer(forward(in)) <-> imapK(layer(in))(forward, backward)
  * }
  * def layerRespectsPure(a: A) = {
  *   layer(a.pure[Inner]) <-> a.pure[M]
  * }
  * def mapIso(ma: M[A])(forward: Inner ~> Inner, backward: Inner ~> Inner) = {
  *   if (forward andThen backward == FunctionK.id[Inner]) {
  *     imapK(ma)(forward, backward) <-> ma
  *   } else {
  *     true
  *   }
  * }
  * }}}
  *
  * `functor.Layer` has one free law, i.e. a law guaranteed by parametricity:
  * {{{
  * def layerRespectsMap(m: Inner[A])(f: A => B) = {
  *   layer(m).map(f) <-> layer(m.map(f))
  * }
  * }}}
  */
trait Layer[M[_], Inner[_]] {
  val outerInstance: Functor[M]
  val innerInstance: Functor[Inner]

  def layer[A](inner: Inner[A]): M[A]

  def layerImapK[A](ma: M[A])(forward: Inner ~> Inner,
                              backward: Inner ~> Inner): M[A]
}
