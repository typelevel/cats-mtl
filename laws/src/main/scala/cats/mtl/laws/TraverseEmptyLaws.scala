package cats
package mtl
package laws

import cats.data.Nested
import cats.laws.IsEq
import cats.laws.IsEqArrow
import cats.syntax.all._
import cats.mtl.syntax.empty._

trait TraverseEmptyLaws[F[_]] extends FunctorEmptyLaws[F] {
  implicit val traverseEmptyInstance: TraverseEmpty[F]

  import cats.mtl.instances.empty.optionTraverseEmpty

  // external laws:
  def traverseFilterIdentity[G[_] : Applicative, A](fa: F[A]): IsEq[G[F[A]]] = {
    fa.traverseFilter(_.some.pure[G]) <-> fa.pure[G]
  }

  def traverseFilterComposition[A, B, C, M[_], N[_]](fa: F[A],
                                                     f: A => M[Option[B]],
                                                     g: B => N[Option[C]]
                                                    )(implicit
                                                      M: Applicative[M],
                                                      N: Applicative[N]
                                                    ): IsEq[Nested[M, N, F[C]]] = {
    val lhs = Nested[M, N, F[C]](fa.traverseFilter(f).map(_.traverseFilter(g)))
    val rhs: Nested[M, N, F[C]] = fa.traverseFilter[NestedC[M, N]#l, C](a =>
      Nested[M, N, Option[C]](f(a).map(_.traverseFilter(g)))
    )
    lhs <-> rhs
  }

  // internal law:
  def filterAConsistentWithTraverseFilter[G[_] : Applicative, A](fa: F[A], f: A => G[Boolean]): IsEq[G[F[A]]] = {
    traverseEmptyInstance.filterA(fa)(f) <-> fa.traverseFilter(a => f(a).map(if (_) Some(a) else None))
  }
}

object TraverseEmptyLaws {
  def apply[F[_]](implicit traverseEmptyInstance0: TraverseEmpty[F]): TraverseEmptyLaws[F] = {
    new TraverseEmptyLaws[F] {
      override lazy val traverseEmptyInstance: TraverseEmpty[F] = traverseEmptyInstance0
      override lazy val functorEmptyInstance: FunctorEmpty[F] = traverseEmptyInstance0.functorEmpty
    }
  }
}
