package cats
package mtl

trait MonadLayer[M[_], Inner[_]] {

  val monad: Monad[M]
  val innerMonad: Monad[Inner]

  def layer[A](inner: Inner[A]): M[A]
  def imapK[A](ma: M[A])(forward: Inner ~> Inner, backward: Inner ~> Inner): M[A]
}
