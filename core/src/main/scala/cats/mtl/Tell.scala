package cats
package mtl

trait Tell[F[_], L] {
  val monad: Monad[F]

  def tell(l: L): F[Unit]

  def writer[A](a: A, l: L): F[A]

}
