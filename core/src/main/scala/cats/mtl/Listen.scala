package cats
package mtl

trait Listen[F[_], L] {

  val tell: Tell[F, L]

  def listen[A](fa: F[A]): F[(A, L)]

  def pass[A](fa: F[(A, L => L)]): F[A]

}

