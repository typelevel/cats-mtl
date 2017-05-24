package cats
package mtl

trait Scoping[F[_], E] {
  val ask: Asking[F, E]

  def local[A](fa: F[A])(f: E => E): F[A]

  def scope[A](fa: F[A])(e: E): F[A]
}

