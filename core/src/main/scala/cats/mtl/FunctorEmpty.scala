package cats
package mtl

/**
  * `FunctorEmpty` has no external laws not guaranteed by parametricity.
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
  * guaranteed by:
  *   empty[X] <-> empty[F[Y]](ex) // parametricity
  *   empty[X].map(f) <-> empty[F[Y]](ex)  // map must have no effect by parametricity, because there's no value inside
  *   empty[X].map(f).join <-> empty[F[Y]].join // add join to both sides
  *   empty[X].flatMap(f) <-> empty(ex) // join is equal, because there's no inner value to flatten effects from
  *   // QED.
  * }}}
  */
trait FunctorEmpty[F[_]] extends Serializable {
  val functor: Functor[F]

  def empty[A]: F[A]

  def filter[A, B](fa: F[A])(f: A => Option[B])(implicit ev: Monad[F]): F[B]
}

object FunctorEmpty {
  def apply[F[_]](implicit functorEmpty: FunctorEmpty[F]): FunctorEmpty[F] = functorEmpty
}