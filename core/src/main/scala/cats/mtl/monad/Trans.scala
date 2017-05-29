package cats
package mtl
package monad

import cats.~>

trait Trans[M[_], Inner[_]] extends Layer[M, Inner] {
  type Outer[F[_], A]

  def showLayers[F[_], A](ma: F[M[A]]): F[Outer[Inner, A]]

  def hideLayers[F[_], A](foia: F[Outer[Inner, A]]): F[M[A]]

  def transInvMap[N[_], NInner[_], A](ma: M[A])
                                     (forward: Inner ~> NInner,
                                      backward: NInner ~> Inner)(implicit other: Trans.AuxIO[N, NInner, Outer]): N[A]
}

object Trans {
  type AuxIO[M[_], Inner0[_], Outer0[_[_], _]] =
    Trans[M, Inner0] {
      type Outer[F[_], A] = Outer0[F, A]
    }
}

