package cats
package mtl

trait MonadTrans[M[_], Inner[_]] extends MonadLayer[M, Inner] {
  type Outer[F[_], A]

  def showLayers[F[_], A](ma: F[M[A]]): F[Outer[Inner, A]]

  def hideLayers[F[_], A](foia: F[Outer[Inner, A]]): F[M[A]]

  def transInvMap[N[_], NInner[_], A](ma: M[A])
                                     (forward: Inner ~> NInner,
                                      backward: NInner ~> Inner)(implicit other: MonadTrans.AuxIO[N, NInner, Outer]): N[A]
}

object MonadTrans {
  type AuxIO[M[_], Inner0[_], Outer0[_[_], _]] =
    MonadTrans[M, Inner0] {
      type Outer[F[_], A] = Outer0[F, A]
    }
}

