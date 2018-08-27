package cats
package mtl

/**
  * `TraverseEmpty`, also known as `Witherable`, represents list-like structures
  * that can essentially have a `traverse` and a `filter` applied as a single
  * combined operation (`traverseFilter`).
  *
  * `TraverseEmpty` has two external laws:
  * {{{
  * def traverseFilterIdentity[G[_]: Applicative, A](fa: F[A]) = {
  *   fa.traverseFilter(_.some.pure[G]) <-> fa.pure[G]
  * }
  *
  * def traverseFilterComposition[A, B, C, M[_], N[_]](fa: F[A],
  *                                                    f: A => M[Option[B]],
  *                                                    g: B => N[Option[C]]
  *                                                   )(implicit
  *                                                     M: Applicative[M],
  *                                                     N: Applicative[N]
  *                                                   ) = {
  *   val lhs = Nested[M, N, F[C]](fa.traverseFilter(f).map(_.traverseFilter(g)))
  *   val rhs: Nested[M, N, F[C]] = fa.traverseFilter[NestedC[M, N]#l, C](a =>
  *     Nested[M, N, Option[C]](f(a).map(_.traverseFilter(g)))
  *   )
  *   lhs <-> rhs
  * }
  * }}}
  *
  *
  * `TraverseEmpty` has one internal law:
  * {{{
  *   def filterAConsistentWithTraverseFilter[G[_]: Applicative, A](fa: F[A], f: A => G[Boolean]) = {
  *     filterA(fa)(f) <-> fa.traverseFilter(a => G.map(f(a))(if (_) Some(a) else None))
  *   }
  * }}}
  * Based on Haskell's [[https://hackage.haskell.org/package/witherable-0.1.3.3/docs/Data-Witherable.html Data.Witherable]]
  */

trait TraverseEmpty[F[_]] extends FunctorEmpty[F] with Serializable {

  val traverse: Traverse[F]

  def traverseFilter[G[_] : Applicative, A, B](fa: F[A])(f: A => G[Option[B]]): G[F[B]]

  def filterA[G[_], A](fa: F[A])(f: A => G[Boolean])(implicit G: Applicative[G]): G[F[A]]
}

object TraverseEmpty {
  def apply[F[_]](implicit traverseEmpty: TraverseEmpty[F]): TraverseEmpty[F] = traverseEmpty
}

trait DefaultTraverseEmpty[F[_]] extends DefaultFunctorEmpty[F] with TraverseEmpty[F] {
  def filterA[G[_], A](fa: F[A])(f: A => G[Boolean])(implicit G: Applicative[G]): G[F[A]] =
    traverseFilter(fa)(a => G.map(f(a))(if (_) Some(a) else None))
}
