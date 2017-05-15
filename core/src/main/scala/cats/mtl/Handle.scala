package cats
package mtl

trait Handle[F[_], E] {
  val raise: Raise[F, E]

  def materialize[A](fa: F[A]): F[E Either A]
  def handleErrorWith[A](fa: F[A])(f: PartialFunction[E, A]): F[A]
}

