package cats
package mtl
package syntax

trait ListenSyntax {
  implicit def toListenOps[F[_], A](fa: F[A]): ListenOps[F, A] = new ListenOps(fa)
  implicit def toPassOps[F[_], L, A](fa: F[(A, L => L)]): PassOps[F, L, A] = new PassOps(fa)
}

final class ListenOps[F[_], A](val fa: F[A]) extends AnyVal {
  def listen[L]()(implicit listen: FunctorListen[F, L]): F[(A, L)] = {
    listen.listen(fa)
  }
}

final class PassOps[F[_], L, A](val fa: F[(A, L => L)]) extends AnyVal {
  def pass()(implicit pass: FunctorListen[F, L]): F[A] = {
    pass.pass(fa)
  }
}

object listen extends ListenSyntax
