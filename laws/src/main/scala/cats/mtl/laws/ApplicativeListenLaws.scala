package cats
package mtl
package laws

import cats.syntax.functor._

trait ApplicativeListenLaws[F[_], L] {
  implicit val monoid: Monoid[L]
  implicit val listen: ApplicativeListen[F, L]
  implicit val tell: ApplicativeTell[F, L] = listen.tell
  implicit val applicative: Applicative[F] = tell.applicative

  // external laws
  def listenRespectsTell(l: L): IsEq[F[(Unit, L)]] = {
    ApplicativeListen.listen(ApplicativeTell.tell(l)) <-> ApplicativeTell.tell(l).map(_ => ((), l))
  }

  def listenAddsNoEffects[A](fa: F[A]): IsEq[F[A]] = {
    ApplicativeListen.listen(fa).map(_._1) <-> fa
  }

  // internal laws:
  def listensIsListenThenMap[A, B](fa: F[A], f: L => B): IsEq[F[(B, A)]] = {
    ApplicativeListen.listens(fa)(f) <-> ApplicativeListen.listen(fa).map { case (a, l) => (f(l), a) }
  }

  def censorIsPassTupled[A](fa: F[A])(f: L => L): IsEq[F[A]] = {
    ApplicativeListen.censor(fa)(f) <-> ApplicativeListen.pass(fa.map(a => (a, f)))
  }
}

