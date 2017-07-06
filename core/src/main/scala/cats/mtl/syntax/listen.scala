package cats
package mtl
package syntax

trait ListenSyntax {
  implicit def toListenOps[F[_], A](fa: F[A]): ListenOps[F, A] = new ListenOps(fa)
  implicit def toPassOps[F[_], L, A](fa: F[(A, L => L)]): PassOps[F, L, A] = new PassOps(fa)
}

final class ListenOps[F[_], A](val fa: F[A]) extends AnyVal {
  def listen[L](implicit listen: FunctorListen[F, L]): F[(A, L)] = {
    listen.listen(fa)
  }

  def listens[L, B](f: L => B)(implicit listen: FunctorListen[F, L]): F[(A, B)] = {
    listen.listens(fa)(f)
  }

  def censor[L](f: L => L)(implicit listen: FunctorListen[F, L]): F[A] = {
    listen.censor(fa)(f)
  }
}

final class PassOps[F[_], L, A](val fa: F[(A, L => L)]) extends AnyVal {
  def pass(implicit pass: FunctorListen[F, L]): F[A] = {
    pass.pass(fa)
  }
}

object listen extends ListenSyntax
