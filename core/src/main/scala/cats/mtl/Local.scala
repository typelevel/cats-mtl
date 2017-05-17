package cats
package mtl

trait Local[F[_], E] {
  val ask: Ask[F, E]

  def local[A](fa: F[A])(f: E => E): F[A]

  def scope[A](fa: F[A])(e: E): F[A]
}

