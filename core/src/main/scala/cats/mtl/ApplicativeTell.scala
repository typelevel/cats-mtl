package cats
package mtl

/**
  * `ApplicativeTell` has two external laws:
  * {{{
  * def tellTwiceIsTellCombined(l1: L, l2: L) = {
  *   tell(l1) *> tell(l2) <-> tell(l1 |+| l2)
  * }
  *
  * def tellZeroIsPureUnit = {
  *   tell(L.zero) <-> pure(())
  * }
  * }}}
  *
  * `ApplicativeTell` has one internal law:
  * {{{
  * def writerIsTellAndMap(a: A, l: L) = {
  *   tell(l).map(_ => a) <-> writer(a, l)
  * }
  *
  * def tupleIsWriter(a: A, l: L) = {
  *   writer(a, l) <-> tuple((l, a))
  * }
  * }}}
  */
trait ApplicativeTell[F[_], L] {
  val applicative: Applicative[F]
  val monoid: Monoid[L]

  def tell(l: L): F[Unit]

  def writer[A](a: A, l: L): F[A]

  def tuple[A](ta: (L, A)): F[A]
}

object ApplicativeTell {
  def tell[F[_], L](l: L)(implicit tell: ApplicativeTell[F, L]): F[Unit] = tell.tell(l)

  def tellF[F[_]] = new tellFPartiallyApplied[F]

  def tellL[L] = new tellLPartiallyApplied[L]

  final private[mtl] class tellLPartiallyApplied[L](val dummy: Boolean = false) extends AnyVal {
    @inline def apply[F[_]](l: L)(implicit tell: ApplicativeTell[F, L]): F[Unit] = {
      tell.tell(l)
    }
  }

  final private[mtl] class tellFPartiallyApplied[F[_]](val dummy: Boolean = false) extends AnyVal {
    @inline def apply[L](l: L)(implicit tell: ApplicativeTell[F, L]): F[Unit] = {
      tell.tell(l)
    }
  }

  abstract class ApplicativeTellTemplate[F[_], L](implicit override val applicative: Applicative[F]) extends ApplicativeTell[F, L]
}

