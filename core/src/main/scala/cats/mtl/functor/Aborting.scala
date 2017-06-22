package cats
package mtl
package functor

/**
  * Aborting has no external laws not guaranteed by parametricity.
  *
  * Aborting has one internal law:
  * {{{
  * def filterIsFlatMapOrAbort(fa: F[A])(f: A => Option[B]) = {
  *   filter(fa)(f) <-> for {
  *     a <- fa
  *     b <- f(a).fold(abort[B])(pure)
  *   } yield b
  * }}}
  *
  * Aborting has one free law, i.e. a law guaranteed by parametricity:
  * {{{
  * def abortThenFlatMapAborts(f: A => F[B]) = {
  *   abort[A].flatMap(f) <-> abort[B]
  * }
  * guaranteed by:
  *   abort[X] <-> abort[F[Y]](ex) // parametricity
  *   abort[X].map(f) <-> abort[F[Y]](ex)  // map must have no effect by parametricity, because there's no value inside
  *   abort[X].map(f).join <-> abort[F[Y]].join // add join to both sides
  *   abort[X].flatMap(f) <-> abort(ex) // join is equal, because there's no inner value to flatten effects from
  *   // QED.
  * }}}
  */
trait Aborting[F[_]] {
  val functor: Functor[F]

  def abort[A]: F[A]

  def filter[A, B](fa: F[A])(f: A => Option[B])(implicit ev: Monad[F]): F[B]
}
