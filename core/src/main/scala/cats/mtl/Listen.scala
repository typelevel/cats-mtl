package cats
package mtl

import cats.mtl.evidence.Nat
import cats.syntax.flatMap._

trait ListenN[N <: Nat, F[_], L] {
  val tell: TellN[N, F, L]
  def listen[A](fa: F[A]): F[(A, L)]
  def pass[A](fa: F[(A, L => L)])(implicit F: Monad[F]): F[A] =
    listen(fa)
      .flatMap { case ((a, f), l) => tell.writer(a, f(l)) }

}

object ListenN {

}
