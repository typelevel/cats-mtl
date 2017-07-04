package cats
package mtl

trait TraverseEmpty[F[_]] extends Traverse[F] with Serializable {

  val functorEmpty: FunctorEmpty[F]

  def traverseFilter[G[_]: Applicative, A, B](fa: F[A])(f: A => G[Option[B]]): G[F[B]]

}
