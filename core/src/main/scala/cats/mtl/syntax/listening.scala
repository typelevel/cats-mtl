package cats
package mtl
package syntax

import cats.mtl.applicative.Listening

trait ListeningSyntax {
  implicit def toListeningOps[F[_], A](fa: F[A]): ListeningOps[F, A] = new ListeningOps(fa)

  implicit def toPassingOps[F[_], L, A](fa: F[(A, L => L)]): PassingOps[F, L, A] = new PassingOps(fa)
}

final class ListeningOps[F[_], A](val fa: F[A]) extends AnyVal {
  def listen[L](implicit listening: Listening[F, L]): F[(A, L)] = {
    listening.listen(fa)
  }
}

final class PassingOps[F[_], L, A](val fa: F[(A, L => L)]) extends AnyVal {
  def pass(implicit passing: Listening[F, L]): F[A] = {
    passing.pass(fa)
  }
}

object listening extends ListeningSyntax
