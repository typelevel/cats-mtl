package cats
package mtl

trait Raise[F[_], E] {
  val monad: Monad[F]

  def raiseError[A](e: E): F[A]
}
