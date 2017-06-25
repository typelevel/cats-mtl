package cats
package mtl
package laws

import cats.syntax.functor._

class ApplicativeListenLaws[F[_], L](implicit val listenInstance: ApplicativeListen[F, L]) {
  implicit val monoid: Monoid[L] = listenInstance.tell.monoid
  implicit val applicative: Applicative[F] = listenInstance.tell.applicative
  
  import listenInstance.{listen, listens, censor, pass}
  import listenInstance.tell._

  // external laws
  def listenRespectsTell(l: L): IsEq[F[(Unit, L)]] = {
    listen(tell(l)) <-> tell(l).map(_ => ((), l))
  }

  def listenAddsNoEffects[A](fa: F[A]): IsEq[F[A]] = {
    listen(fa).map(_._1) <-> fa
  }

  // internal laws:
  def listensIsListenThenMap[A, B](fa: F[A], f: L => B): IsEq[F[(B, A)]] = {
    listens(fa)(f) <-> listen(fa).map { case (a, l) => (f(l), a) }
  }

  def censorIsPassTupled[A](fa: F[A])(f: L => L): IsEq[F[A]] = {
    censor(fa)(f) <-> pass(fa.map(a => (a, f)))
  }
}

object ApplicativeListenLaws {
  def apply[F[_], E](implicit listenInstance: ApplicativeListen[F, E]): ApplicativeListenLaws[F, E] = {
    new ApplicativeListenLaws()
  }
}
