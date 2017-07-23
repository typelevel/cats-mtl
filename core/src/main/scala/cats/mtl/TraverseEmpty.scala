package cats
package mtl

/**
  * `TraverseEmpty`, also known as `Witherable`, represents list-like structures
  * that can essentially have a `traverse` and a `filter` applied as a single
  * combined operation (`traverseFilter`).
  *
  * Must obey the laws defined in cats.laws.TraverseFilterLaws.
  *
  * Based on Haskell's [[https://hackage.haskell.org/package/witherable-0.1.3.3/docs/Data-Witherable.html Data.Witherable]]
  */

trait TraverseEmpty[F[_]] extends Traverse[F] with Serializable {

  val functorEmpty: FunctorEmpty[F]

  def traverseFilter[G[_]: Applicative, A, B](fa: F[A])(f: A => G[Option[B]]): G[F[B]]

  def mapFilter[A, B](fa: F[A])(f: A => Option[B]): F[B]

  def filterA[G[_], A](fa: F[A])(f: A => G[Boolean])(implicit G: Applicative[G]): G[F[A]]

  def filter[A](fa: F[A])(f: A => Boolean): F[A]

}
