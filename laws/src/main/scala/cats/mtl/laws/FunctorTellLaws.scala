package cats
package mtl
package laws

import cats.laws.IsEq
import cats.laws.IsEqArrow
import cats.syntax.functor._

trait FunctorTellLaws[F[_], L] {
  implicit val tellInstance: FunctorTell[F, L]
  implicit val functor: Functor[F] = tellInstance.functor

  import tellInstance._

  // internal laws:
  def writerIsTellAndMap[A](a: A, l: L): IsEq[F[A]] = {
    (tell(l) as a) <-> writer(a, l)
  }

  def tupleIsWriterFlipped[A](a: A, l: L): IsEq[F[A]] = {
    writer(a, l) <-> tuple((l, a))
  }

}

object FunctorTellLaws {
  def apply[F[_], L](implicit instance0: FunctorTell[F, L]): FunctorTellLaws[F, L] = {
    new FunctorTellLaws[F, L] {
      override lazy val tellInstance: FunctorTell[F, L] = instance0
    }
  }
}
