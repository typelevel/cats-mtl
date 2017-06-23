package cats
package mtl
package laws

import cats.mtl.{ApplicativeListen, ApplicativeTell}
import cats.syntax.all._

trait ListeningLaws[F[_], L] {
  implicit val monoid: Monoid[L]
  implicit val listening: ApplicativeListen[F, L]
  implicit val telling: ApplicativeTell[F, L] = listening.tell
  implicit val applicative: Applicative[F] = telling.applicative

  // external laws
  def listenRespectsTell(l: L) = {
    ApplicativeListen.listen(ApplicativeTell.tell(l)) <-> ApplicativeTell.tell(l).map(_ => ((), l))
  }

  def listenAddsNoEffects[A](fa: F[A]) = {
    ApplicativeListen.listen(fa).map(_._1) <-> fa
  }

  // internal laws:
  def listensIsListenThenMap[A, B](fa: F[A], f: L => B) = {
    ApplicativeListen.listens(fa)(f) <-> ApplicativeListen.listen(fa).map { case (a, l) => (f(l), a) }
  }

  def censorIsPassTupled[A](fa: F[A])(f: L => L) = {
    ApplicativeListen.censor(fa)(f) <-> ApplicativeListen.pass(fa.map(a => (a, f)))
  }
}
