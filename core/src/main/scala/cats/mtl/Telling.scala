package cats
package mtl

trait Telling[F[_], L] {
  def tell(l: L): F[Unit]

  def writer[A](a: A, l: L): F[A]
}
