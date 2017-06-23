package cats
package mtl
package laws

import cats.syntax.all._

trait TellingLaws[F[_], L] {
  implicit val monoid: Monoid[L]
  implicit val telling: ApplicativeTell[F, L]
  implicit val applicative: Applicative[F] = telling.applicative

  // external laws
  def tellTwiceIsTellCombined(l1: L, l2: L): IsEq[F[Unit]] = {
    (telling.tell(l1) |@| telling.tell(l2)).tupled.void <-> telling.tell(monoid.combine(l1, l2))
  }

  def tellEmptyIsPureUnit: IsEq[F[Unit]] = {
    telling.tell(monoid.empty) <-> applicative.pure(())
  }

  // internal laws
  def writerIsTellAndMap[A](a: A, l: L): IsEq[F[A]] = {
    telling.tell(l).map(_ => a) <-> telling.writer(a, l)
  }

  def tupleIsWriter[A](a: A, l: L): IsEq[F[A]] = {
    telling.writer(a, l) <-> telling.tuple((l, a))
  }

}
