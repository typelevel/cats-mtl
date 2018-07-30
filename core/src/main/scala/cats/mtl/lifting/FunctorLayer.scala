package cats
package mtl
package lifting

import cats.{Functor, ~>}

/**
  * `FunctorLayer[M, Inner]` has the following functionality:
  * - lifts values from the `Functor` `Inner` to the `Functor` `M`.
  * - lifts `Functor` isomorphisms in `Inner` (`(Inner ~> Inner, Inner ~> Inner)`)
  *   into `Functor` homomorphisms in `M` (`M ~> M`).
  *   This allows you to "map" a natural transformation over the `Inner` inside `M`,
  *   but only if you can provide an inverse of that natural transformation.
  *
  * `FunctorLayer[M, Inner]` has two external laws:
  * {{{
  * def mapForwardRespectsLayer[A](in: Inner[A])(forward: Inner ~> Inner, backward: Inner ~> Inner) = {
  *   layer(forward(in)) <-> layerImapK(layer(in))(forward, backward)
  * }
  *
  * def layerImapRespectsId[A](in: M[A]) = {
  *   in <-> layerImapK(in)(FunctionK.id, FunctionK.id)
  * }
  * }}}
  *
  * `FunctorLayer` has one free law, i.e. a law guaranteed by parametricity:
  * {{{
  * def layerRespectsMap[A, B](in: Inner[A])(f: A => B) = {
  *   layer(in.map(f)) <-> layer(in).map(f)
  * } // this is free because all `FunctionK`'s (including `layer`, despite not being an instance of the class)
  *   // are natural transformations (i.e., they follow this law exactly)
  * }}}
  */
trait FunctorLayer[M[_], Inner[_]] extends Serializable {
  val outerInstance: Functor[M]
  val innerInstance: Functor[Inner]

  def layer[A](inner: Inner[A]): M[A]

  def layerImapK[A](ma: M[A])(forward: Inner ~> Inner,
                              backward: Inner ~> Inner): M[A]
}

object FunctorLayer {
  def apply[M[_], Inner[_]](implicit functorLayer: FunctorLayer[M, Inner]): FunctorLayer[M, Inner] = functorLayer
}
