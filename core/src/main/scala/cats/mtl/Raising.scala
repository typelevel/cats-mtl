package cats
package mtl

trait Raising[F[_], E] {
  def raiseError[A](e: E): F[A]
}
