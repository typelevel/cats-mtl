package cats
package mtl

/**
  * `FunctorEmpty[F]` denotes the unique empty value of type `F[A]`.
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
  * `FunctorEmpty` has one internal law:
  * {{{
  * def filterIsFlatMapOrAbort(fa: F[A])(f: A => Option[B])(implicit ev: Monad[F]) = {
  *   filter(fa)(f) <-> for {
  *     a <- fa
  *     b <- f(a).fold(empty[B])(pure)
  *   } yield b
  * }}}
  *
  * `FunctorEmpty` has one free law, i.e. a law guaranteed by parametricity:
  * {{{
  * def emptyThenFlatMapAborts(f: A => F[B]) = {
  *   empty[A].flatMap(f) <-> empty[B]
  * }
  * }}}
  * guaranteed by:
  * empty[X] <-> empty[F[Y]](ex) // parametricity
  * empty[X].map(f) <-> empty[F[Y]](ex)  // map must have no effect by parametricity, because there's no value inside
  * empty[X].map(f).join <-> empty[F[Y]].join // add join to both sides
  * empty[X].flatMap(f) <-> empty(ex) // join is equal, because there's no inner value to flatten effects from
  * // QED.
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
