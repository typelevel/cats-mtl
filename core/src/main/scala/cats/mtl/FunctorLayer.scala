package cats
package mtl

/**
  * `FunctorLayer[M, Inner]` has external one law:
  * {{{
  * def mapForwardRespectsLayer[A](in: Inner[A])(forward: Inner ~> Inner, backward: Inner ~> Inner) = {
  *   layer(forward(in)) <-> imapK(layer(in))(forward, backward)
  * }
  * }}}
  *
  * `FunctorLayer` has one free law, i.e. a law guaranteed by parametricity:
  * {{{
  * def layerRespectsMap[A](m: Inner[A])(f: A => B) = {
  *   layer(m).map(f) <-> layer(m.map(f))
  * }
  * }}}
  */
trait FunctorLayer[M[_], Inner[_]] extends Serializable {
  val outerInstance: Functor[M]
  val innerInstance: Functor[Inner]

  def layer[A](inner: Inner[A]): M[A]

  def layerImapK[A](ma: M[A])(forward: Inner ~> Inner,
                              backward: Inner ~> Inner): M[A]
}
