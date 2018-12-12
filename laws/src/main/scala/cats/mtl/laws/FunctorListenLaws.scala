package cats
package mtl
package laws

import cats.laws.IsEq
import cats.laws.IsEqArrow
import cats.syntax.functor._

trait FunctorListenLaws[F[_], L] extends FunctorTellLaws[F, L] {
  implicit def F: FunctorListen[F, L]

  // external laws:
  def listenRespectsTell(l: L): IsEq[F[(Unit, L)]] = {
    F.listen(F.tell(l)) <-> F.tell(l).as(((), l))
  }

  def listenAddsNoEffects[A](fa: F[A]): IsEq[F[A]] = {
    F.listen(fa).map(_._1) <-> fa
  }

  // internal law:
  def listensIsListenThenMap[A, B](fa: F[A], f: L => B): IsEq[F[(A, B)]] = {
    F.listens(fa)(f) <-> F.listen(fa).map { case (a, l) => (a, f(l)) }
  }
}

object FunctorListenLaws {
  def apply[F[_], E](implicit instance0: FunctorListen[F, E]): FunctorListenLaws[F, E] = {
    new FunctorListenLaws[F, E] {
      def F: FunctorListen[F, E] = instance0
    }
  }
}
