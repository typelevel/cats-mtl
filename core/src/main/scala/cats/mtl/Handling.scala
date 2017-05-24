package cats
package mtl

trait Handling[F[_], E] {
  val raise: Raising[F, E]

  def materialize[A](fa: F[A]): F[E Either A]
  def handleErrorWith[A](fa: F[A])(f: PartialFunction[E, A]): F[A]
}

