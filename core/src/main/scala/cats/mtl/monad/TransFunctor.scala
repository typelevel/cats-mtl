package cats
package mtl
package monad

import cats.~>

/**
  * laws:
  * {{{
  *   def
  * }}}
  */
trait TransFunctor[M[_], Inner[_]] extends LayerFunctor[M, Inner] with Trans[M, Inner] {
  def transMap[A, N[_], NInner[_]](ma: M[A])(trans: Inner ~> NInner)
                                  (implicit mt: Trans.AuxIO[N, NInner, Outer]): N[A]

}

object TransFunctor {
  type Aux[M[_], Inner[_], Outer0[_[_], _]] =
    TransFunctor[M, Inner] {type Outer[F[_], A] = Outer0[F, A]}
}
