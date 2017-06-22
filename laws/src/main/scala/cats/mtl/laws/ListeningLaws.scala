package cats
package mtl
package laws

import cats.mtl.applicative.{Listening, Telling}
import cats.syntax.all._

trait ListeningLaws[F[_], L] {
  implicit val monoid: Monoid[L]
  implicit val listening: Listening[F, L]
  implicit val telling: Telling[F, L] = listening.tell
  implicit val applicative: Applicative[F] = telling.applicative

  // external laws
  def listenRespectsTell(l: L) = {
    Listening.listen(Telling.tell(l)) <-> Telling.tell(l).map(_ => ((), l))
  }

  def listenAddsNoEffects[A](fa: F[A]) = {
    Listening.listen(fa).map(_._1) <-> fa
  }

  // internal laws:
  def listensIsListenThenMap[A, B](fa: F[A], f: L => B) = {
    Listening.listens(fa)(f) <-> Listening.listen(fa).map { case (a, l) => (f(l), a) }
  }

  def censorIsPassTupled[A](fa: F[A])(f: L => L) = {
    Listening.censor(fa)(f) <-> Listening.pass(fa.map(a => (a, f)))
  }
}
