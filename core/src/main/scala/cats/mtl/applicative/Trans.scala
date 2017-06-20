package cats
package mtl
package applicative

/**
  * `applicative.Trans` has two external laws:
  * {{{
  * def transInvMapIdentity(ma: M[A]) = {
  *   transInvMap(ma)(FunctionK.id, FunctionK.id) == ma
  * }
  * def transInvMapComposition[N[_], NInner[_]](ma: M[A], f1: Inner ~> NInner, f2: Inner ~> NInner,
  *                                             b1: NInner ~> Inner, b2: NInner ~> Inner)(implicit other: Tranx.Aux[N, NInner, Outer]) = {
  *   transInvMap(transInvMap(ma)(f2, b2))(f1, b1) == transInvMap(ma)(f1.andThen(b1), b2.andThen(f2))
  * }
  * }}}
  */
trait Trans[M[_], Inner[_]] extends Layer[M, Inner] {
  type Outer[F[_], A]

  def showLayers[F[_], A](ma: F[M[A]]): F[Outer[Inner, A]]

  def hideLayers[F[_], A](foia: F[Outer[Inner, A]]): F[M[A]]

  def transInvMap[N[_], NInner[_], A](ma: M[A])
                                     (forward: Inner ~> NInner,
                                      backward: NInner ~> Inner)(implicit other: Trans.Aux[N, NInner, Outer]): N[A]
}

object Trans {
  type Aux[M[_], Inner0[_], Outer0[_[_], _]] =
    Trans[M, Inner0] {
      type Outer[F[_], A] = Outer0[F, A]
    }
}

