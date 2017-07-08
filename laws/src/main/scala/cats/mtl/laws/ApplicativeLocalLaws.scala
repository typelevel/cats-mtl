package cats
package mtl
package laws

trait ApplicativeLocalLaws[F[_], E] extends ApplicativeAskLaws[F, E] {
  implicit val localInstance: ApplicativeLocal[F, E]
  import localInstance.{local, scope}
  import localInstance.ask._
  import localInstance.ask.applicative._

  // external law:
  def askReflectsLocal(f: E => E): IsEq[F[E]] = {
    local(ask)(f) <-> map(ask)(f)
  }

  // internal law:
  def scopeIsLocalConst[A](fa: F[A], e: E): IsEq[F[A]] = {
    local(fa)(_ => e) <-> scope(fa)(e)
  }

}

object ApplicativeLocalLaws {
  def apply[F[_], E](implicit instance0: ApplicativeLocal[F, E]): ApplicativeLocalLaws[F, E] = {
    new ApplicativeLocalLaws[F, E] {
      lazy val localInstance: ApplicativeLocal[F, E] = instance0
      override lazy val askInstance: ApplicativeAsk[F, E] = instance0.ask
    }
  }
}
