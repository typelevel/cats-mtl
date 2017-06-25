package cats
package mtl
package laws

class ApplicativeLocalLaws[F[_], E]()(implicit val localInstance: ApplicativeLocal[F, E]) {

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
  def apply[F[_], E](implicit localInstance: ApplicativeLocal[F, E]): ApplicativeLocalLaws[F, E] = {
    new ApplicativeLocalLaws()
  }
}