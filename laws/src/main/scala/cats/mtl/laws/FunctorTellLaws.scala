package cats
package mtl
package laws

import cats.laws.IsEq
import cats.laws.IsEqArrow
import cats.syntax.functor._

trait FunctorTellLaws[F[_], L] {
  implicit def F: FunctorTell[F, L]
  implicit def functor(implicit F: FunctorTell[F, L]): Functor[F] = F.functor

  // internal laws:
  def writerIsTellAndMap[A](a: A, l: L): IsEq[F[A]] = {
    F.tell(l).as(a) <-> F.writer(a, l)
  }

  def tupleIsWriterFlipped[A](a: A, l: L): IsEq[F[A]] = {
    F.writer(a, l) <-> F.tuple((l, a))
  }

}

object FunctorTellLaws {
  def apply[F[_], L](implicit instance0: FunctorTell[F, L]): FunctorTellLaws[F, L] = {
    new FunctorTellLaws[F, L] {
      def F: FunctorTell[F, L] = instance0
    }
  }
}
