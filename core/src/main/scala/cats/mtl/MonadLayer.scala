package cats
package mtl

trait MonadLayer[M[_]] {
  type Inner[A]

  val monad: Monad[M]
  val innerMonad: Monad[(Inner of M)#l]

  def layer[A](inner: Inner[A]): M[A]
  def imapK[A](ma: M[A])(forward: Inner ~> Inner, backward: Inner ~> Inner): M[A]
}

object MonadLayer {
  type Aux[M[_], T[_[_], _]] = MonadLayer[M] {type Inner[F[_], A] = T[F, A]}
}
