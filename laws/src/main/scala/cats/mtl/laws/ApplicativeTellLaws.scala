package cats
package mtl
package laws

import cats.syntax.functor._
import cats.syntax.semigroup._
import cats.syntax.cartesian._

class ApplicativeTellLaws[F[_], L]()(implicit val tellInstance: ApplicativeTell[F, L]) {
  implicit val monoid: Monoid[L] = tellInstance.monoid
  implicit val applicative: Applicative[F] = tellInstance.applicative

  import tellInstance._
  import monoid._
  import applicative._

  // external laws
  def tellTwiceIsTellCombined(l1: L, l2: L): IsEq[F[Unit]] = {
    (tell(l1) *> tell(l2)) <-> tell(l1 |+| l2)
  }

  def tellEmptyIsPureUnit: IsEq[F[Unit]] = {
    tell(empty) <-> pure(())
  }

  // internal laws
  def writerIsTellAndMap[A](a: A, l: L): IsEq[F[A]] = {
    (tell(l) as a) <-> writer(a, l)
  }

  def tupleIsWriterFlipped[A](a: A, l: L): IsEq[F[A]] = {
    writer(a, l) <-> tuple((l, a))
  }

}

object ApplicativeTellLaws {
  def apply[F[_], L](implicit tell: ApplicativeTell[F, L]): ApplicativeTellLaws[F, L] = {
    new ApplicativeTellLaws[F, L]()(tell)
  }
}