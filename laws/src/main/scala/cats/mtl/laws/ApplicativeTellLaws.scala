package cats
package mtl
package laws

import cats.syntax.functor._
import cats.syntax.applicative._
import cats.syntax.cartesian._

trait ApplicativeTellLaws[F[_], L] {
  implicit val monoid: Monoid[L]
  implicit val tell: ApplicativeTell[F, L]
  implicit val applicative: Applicative[F] = tell.applicative

  // external laws
  def tellTwiceIsTellCombined(l1: L, l2: L): IsEq[F[Unit]] = {
    (tell.tell(l1) |@| tell.tell(l2)).tupled.void <-> tell.tell(monoid.combine(l1, l2))
  }

  def tellEmptyIsPureUnit: IsEq[F[Unit]] = {
    tell.tell(monoid.empty) <-> ().pure[F]
  }

  // internal laws
  def writerIsTellAndMap[A](a: A, l: L): IsEq[F[A]] = {
    tell.tell(l).map(_ => a) <-> tell.writer(a, l)
  }

  def tupleIsWriter[A](a: A, l: L): IsEq[F[A]] = {
    tell.writer(a, l) <-> tell.tuple((l, a))
  }

}
