package cats
package mtl
package syntax

trait ListenSyntax {
  implicit def toListenOps[F[_], A](fa: F[A]): ListenOps[F, A] = new ListenOps(fa)
}

final class ListenOps[F[_], A](val fa: F[A]) extends AnyVal {
  def listen[L](implicit listen: FunctorListen[F, L]): F[(A, L)] =
    listen.listen(fa)

  def listens[L, B](f: L => B)(implicit listen: FunctorListen[F, L]): F[(A, B)] =
    listen.listens(fa)(f)

}

object listen extends ListenSyntax
