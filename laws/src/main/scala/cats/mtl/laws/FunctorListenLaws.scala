package cats
package mtl
package laws

import cats.laws.IsEq
import cats.laws.IsEqArrow
import cats.syntax.functor._

trait FunctorListenLaws[F[_], L] extends FunctorTellLaws[F, L] {
  implicit val listenInstance: FunctorListen[F, L]
  import listenInstance.{tell, listen, listens}

  // external laws:
  def listenRespectsTell(l: L): IsEq[F[(Unit, L)]] = {
    listen(tell(l)) <-> tell(l).as(((), l))
  }

  def listenAddsNoEffects[A](fa: F[A]): IsEq[F[A]] = {
    listen(fa).map(_._1) <-> fa
  }

  // internal law:
  def listensIsListenThenMap[A, B](fa: F[A], f: L => B): IsEq[F[(A, B)]] = {
    listens(fa)(f) <-> listen(fa).map { case (a, l) => (a, f(l)) }
  }
}

object FunctorListenLaws {
  def apply[F[_], E](implicit instance0: FunctorListen[F, E]): FunctorListenLaws[F, E] = {
    new FunctorListenLaws[F, E] {
      override lazy val listenInstance: FunctorListen[F, E] = instance0
      override lazy val tellInstance: FunctorTell[F, E] = instance0
    }
  }
}
