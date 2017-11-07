package cats
package mtl
package laws

import cats.laws.IsEq
import cats.laws.IsEqArrow
import cats.syntax.apply._
import cats.syntax.functor._

trait ApplicativeAskLaws[F[_], E] {
  implicit val askInstance: ApplicativeAsk[F, E]
  import askInstance._
  implicit val applicative: Applicative[F] = askInstance.applicative

  // external law:
  def askAddsNoEffects[A](fa: F[A]): IsEq[F[A]] = {
    ask *> fa <-> fa
  }

  // internal law:
  def readerIsAskAndMap[A](f: E => A): IsEq[F[A]] = {
    ask.map(f) <-> reader(f)
  }
}

object ApplicativeAskLaws {
  def apply[F[_], E](implicit instance0: ApplicativeAsk[F, E]): ApplicativeAskLaws[F, E] = {
    new ApplicativeAskLaws[F, E] {
      override lazy val askInstance = instance0
    }
  }
}
