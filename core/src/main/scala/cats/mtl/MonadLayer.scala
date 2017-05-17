package cats
package mtl

trait MonadLayer[M[_]] {
  type Inner[A]

  val monad: Monad[M]
  val innerMonad: Monad[Inner]

  def layer[A](inner: Inner[A]): M[A]
  def imapK[A](ma: M[A])(forward: Inner ~> Inner, backward: Inner ~> Inner): M[A]
}

object MonadLayer {
  type Aux[M[_], Inner0[_]] = MonadLayer[M] {type Inner[A] = Inner0[A]}
}
