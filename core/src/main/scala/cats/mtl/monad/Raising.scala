package cats
package mtl
package monad

/**
  * Raising has no laws not guaranteed by parametricity.
  *
  * Raising has one free law, i.e. a law guaranteed by parametricity:
  * {{{
  * def failThenFlatMapFails[A, B](ex: E, f: A => F[B]) = {
  *   fail(ex).flatMap(f) == fail(ex)
  * }
  * guaranteed by:
  *   fail[X](ex) <-> fail[IO[Y]](ex) // parametricity
  *   fail[X](ex).map(f) <-> fail[IO[Y]](ex)  // map must have no effect, because there's no value
  *   fail[X](ex).map(f).join <-> fail[IO[Y]].join // add join to both sides
  *   fail(ex).flatMap(f) <-> fail(ex) // join is equal, because there's no inner value to flatten effects from
  *   // QED.
  * }}}
  */
trait Raising[F[_], E] {
  val monad: Monad[F]

  def raise[A](e: E): F[A]
}
