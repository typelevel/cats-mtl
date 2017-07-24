package cats
package mtl
package laws

trait FunctorEmptyLaws[F[_]] {
  val functorEmptyInstance: FunctorEmpty[F]

  import functorEmptyInstance._

  implicit val functor: Functor[F] = functorEmptyInstance.functor

  // external laws
  def mapFilterComposition[A, B, C](fa: F[A], f: A => Option[B], g: B => Option[C]): IsEq[F[C]] = {
    val lhs: F[C] = mapFilter(mapFilter(fa)(f))(g)
    val rhs: F[C] = mapFilter(fa)(a => f(a).flatMap(g))
    lhs <-> rhs
  }

  def mapFilterMapConsistency[A, B](fa: F[A], f: A => B): IsEq[F[B]] = {
    mapFilter(fa)(f andThen (x => Some(x): Option[B])) <-> functor.map(fa)(f)
  }

}

object FunctorEmptyLaws {
  def apply[F[_]](implicit functorEmpty: FunctorEmpty[F]): FunctorEmptyLaws[F] = {
    new FunctorEmptyLaws[F] {
      override lazy val functorEmptyInstance: FunctorEmpty[F] = functorEmpty
    }
  }
}
