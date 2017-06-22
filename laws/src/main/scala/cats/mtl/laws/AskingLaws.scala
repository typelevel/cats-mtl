package cats
package mtl
package laws

import cats.mtl.applicative.Asking
import cats.syntax.all._

trait AskingLaws[F[_], E] {
  implicit val asking: Asking[F, E]
  implicit val applicative: Applicative[F] = asking.applicative

  // external laws
  def askAddsNoEffectsAndIsNotAffected[A](fa: F[A]): IsEq[F[A]] = {
    (Asking.ask *> fa) <-> fa
  }

  def askIsNotAffected[A](fa: F[A]): IsEq[F[E]] = {
    (fa *> Asking.ask) <-> (Asking.ask <* fa)
  }

  // internal laws
  def readerIsAskAndMap[A](f: E => A): IsEq[F[A]] = {
    Asking.ask.map(f) <-> Asking.reader(f)
  }
}
