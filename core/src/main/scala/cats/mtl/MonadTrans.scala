package cats
package mtl

trait MonadTrans[M[_]] extends MonadLayer[M] {
  type Outer[F[_], A]

  def showLayers[F[_], A](ma: F[M[A]]): F[Outer[Inner, A]]

  def hideLayers[F[_], A](foia: F[Outer[Inner, A]]): F[M[A]]

  def transInvMap[N[_], NInner[_], A](ma: M[A])
                                     (forward: Inner ~> NInner,
                                      backward: NInner ~> Inner)(implicit other: MonadTrans.AuxIO[N, NInner, Outer]): N[A]
}

object MonadTrans {
  type AuxO[M[_], Outer0[_[_], _]] = MonadTrans[M] {type Outer[F[_], A] = Outer0[F, A]}
  type AuxI[M[_], Inner0[_]] = MonadTrans[M] {type Inner[A] = Inner0[A]}
  type AuxIO[M[_], Inner0[_], Outer0[_[_], _]] =
    MonadTrans[M] {
      type Inner[A] = Inner0[A]
      type Outer[F[_], A] = Outer0[F, A]
    }
}

