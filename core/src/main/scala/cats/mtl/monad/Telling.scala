package cats
package mtl
package monad

/**
  * Telling has one external law:
  * {{{
  * def tellTwiceIsTellCombined(l1: L, l2: L) = {
  *   tell(l1) >> tell(l2) == tell(l1 |+| l2)
  * } // monoid homomorphism
  * }}}
  *
  * Telling has one internal law:
  * {{{
  * def writerIsTellAndMap(a: A, l: L) = {
  *   tell(l).map(_ => a) == writer(a, l)
  * }
  * }}}
  */
trait Telling[F[_], L] {
  val monad: Monad[F]
  val monoid: Monoid[L]

  def tell(l: L): F[Unit]

  def writer[A](a: A, l: L): F[A]
}

abstract class TellingTemplate[F[_], L](implicit override val monad: Monad[F]) extends Telling[F, L]
