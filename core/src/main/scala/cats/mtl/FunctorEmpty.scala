package cats
package mtl

/**
  * `FunctorEmpty[F]` allows you to `map` and filter out elements simultaneously.
  *
  * `FunctorEmpty` has two external laws:
  * {{{
  * def mapFilterComposition[A, B, C](fa: F[A], f: A => Option[B], g: B => Option[C]) = {
  *   val lhs: F[C] = fa.mapFilter(f).mapFilter(g)
  *   val rhs: F[C] = fa.mapFilter(a => f(a).flatMap(g))
  *   lhs <-> rhs
  * }
  *
  * def mapFilterMapConsistency[A, B](fa: F[A], f: A => B) = {
  *   fa.mapFilter(f andThen (_.some)) <-> fa.map(f)
  * }
  * }}}
  *
  * `FunctorEmpty` has three internal laws:
  * {{{
  * def collectConsistentWithMapFilter[A, B](fa: F[A], f: PartialFunction[A, B]) = {
  *   collect(fa)(f) <-> mapFilter(fa)(f.lift)
  * }
  *
  * def flattenOptionConsistentWithMapFilter[A](fa: F[Option[A]]) = {
  *   flattenOption(fa) <-> mapFilter(fa)(identity)
  * }
  *
  * def filterConsistentWithMapFilter[A](fa: F[A], f: A => Boolean) = {
  *   filter(fa)(f) <->
  *     mapFilter(fa)(a => if (f(a)) Some(a) else None)
  * }
  * }}}
  */
trait FunctorEmpty[F[_]] extends Serializable {
  val functor: Functor[F]

  def mapFilter[A, B](fa: F[A])(f: A => Option[B]): F[B]

  def collect[A, B](fa: F[A])(f: PartialFunction[A, B]): F[B]

  def flattenOption[A](fa: F[Option[A]]): F[A]

  def filter[A](fa: F[A])(f: A => Boolean): F[A]
}

object FunctorEmpty {
  def apply[F[_]](implicit functorEmpty: FunctorEmpty[F]): FunctorEmpty[F] = functorEmpty
}


trait DefaultFunctorEmpty[F[_]] extends FunctorEmpty[F] {
  def collect[A, B](fa: F[A])(f: PartialFunction[A, B]): F[B] =
    mapFilter(fa)(f.lift)

  def flattenOption[A](fa: F[Option[A]]): F[A] =
    mapFilter(fa)(identity)

  def filter[A](fa: F[A])(f: A => Boolean): F[A] =
    mapFilter(fa)(a => if (f(a)) Some(a) else None)
}
