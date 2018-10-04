package cats
package mtl
package laws

import cats.laws.IsEq
import cats.laws.IsEqArrow
import cats.syntax.all._

trait ApplicativeCensorLaws[F[_], L] extends FunctorListenLaws[F, L] {
  implicit def F: ApplicativeCensor[F, L]

  implicit def L: Monoid[L] = F.monoid
  implicit def A: Applicative[F] = F.applicative

  def tellRightProductHomomorphism(l1: L, l2: L): IsEq[F[Unit]] =
    (F.tell(l1) *> F.tell(l2)) <-> F.tell(l1 |+| l2)

  def tellLeftProductHomomorphism(l1: L, l2: L): IsEq[F[Unit]] =
    (F.tell(l1) <* F.tell(l2)) <-> F.tell(l1 |+| l2)

  def censorWithPurIsTellEmpty[A](a: A, f: L => L): IsEq[F[A]] =
    F.censor(A.pure(a))(f) <-> F.tell(f(L.empty)).as(a)

  def clearIsIdempotent[A](fa: F[A]): IsEq[F[A]] =
    F.clear(F.clear(fa)) <-> F.clear(fa)

  def tellAndClearIsPureUnit(l: L): IsEq[F[Unit]] =
    F.clear(F.tell(l)) <-> A.pure(())

}

object ApplicativeCensorLaws {
  def apply[F[_], L](implicit instance0: ApplicativeCensor[F, L]): ApplicativeCensorLaws[F, L] = {
    new ApplicativeCensorLaws[F, L] {
      def F: ApplicativeCensor[F, L] = instance0
    }
  }
}
