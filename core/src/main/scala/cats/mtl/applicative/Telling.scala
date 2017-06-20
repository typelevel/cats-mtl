package cats
package mtl
package applicative

/**
  * Telling has two external laws:
  * {{{
  * def tellTwiceIsTellCombined(l1: L, l2: L) = {
  *   tell(l1) *> tell(l2) == tell(l1 |+| l2)
  * }
  *
  * def tellZeroIsPureUnit = {
  *   tell(L.zero) == pure(())
  * }
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
  val applicative: Applicative[F]
  val monoid: Monoid[L]

  def tell(l: L): F[Unit]

  def writer[A](a: A, l: L): F[A]

  def tuple[A](ta: (A, L)): F[A]
}

object Telling {
  def tell[F[_], L](l: L)(implicit telling: Telling[F, L]): F[Unit] = telling.tell(l)

  def tellF[F[_]] = new tellFPartiallyApplied[F]

  def tellL[L] = new tellLPartiallyApplied[L]

  final private[mtl] class tellLPartiallyApplied[L](val dummy: Boolean = false) extends AnyVal {
    @inline def apply[F[_]](l: L)(implicit tell: Telling[F, L]): F[Unit] = {
      tell.tell(l)
    }
  }

  final private[mtl] class tellFPartiallyApplied[F[_]](val dummy: Boolean = false) extends AnyVal {
    @inline def apply[L](l: L)(implicit tell: Telling[F, L]): F[Unit] = {
      tell.tell(l)
    }
  }

}

abstract class TellingTemplate[F[_], L](implicit override val applicative: Applicative[F]) extends Telling[F, L]
