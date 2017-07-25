package cats
package mtl

/**
  * MonadLayerControl is possible to use to access lower monad layers in invariant position,
  * as in so-called "control operations".
  *
  * Three external laws:
  * {{{
  * def layerMapRespectsLayerControl[A](m: M[A], f: Inner ~> Inner) = {
  *   layerMapK(m)(f) <-> layerControl(run => f(run(m)).flatMap(restore)
  * }
  *
  * def distributionLaw[A](nt: State ~> State, st: State[A]) = {
  *   restore(nt(st)) <-> layerControl[State[A]](_ (restore(st)).map(nt(_))).flatMap(restore)
  * }
  *
  * def layerControlIdentity[A](ma: M[A]) = {
  *   ma <->
  *     layerControl[Inner[State[A]]] { cps =>
  *       cps(ma).pure[Inner]
  *     }.flatMap(layer).flatMap(restore)
  * }
  * }}}
  */
trait MonadLayerControl[M[_], Inner[_]] extends MonadLayerFunctor[M, Inner] {

  type State[A]

  def restore[A](state: State[A]): M[A]

  def layerControl[A](cps: (M ~> (Inner of State)#l) => Inner[A]): M[A]

  def zero[A](state: State[A]): Boolean

}

object MonadLayerControl {
  type Aux[M[_], Inner0[_], State0[_]] = MonadLayerControl[M, Inner0] {
    type State[A] = State0[A]
  }

  def apply[M[_], Inner[_]](implicit monadLayerControl: MonadLayerControl[M, Inner]): MonadLayerControl[M, Inner] = monadLayerControl
}

