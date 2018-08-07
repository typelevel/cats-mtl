package cats
package mtl
package syntax

import cats.data.Ior

trait ChronicleSyntax {
  implicit def toChronicleOps[F[_], A](fa: F[A]): ChronicleOps[F, A] = new ChronicleOps[F, A](fa)
}

final class ChronicleOps[F[_], A](val fa: F[A]) extends AnyVal {
  def dictate[E](e: E)(implicit chronicle: MonadChronicle[F, E]): F[Unit] = {
    chronicle.dictate(e)
  }

  def disclose[E](e: E)(implicit chronicle: MonadChronicle[F, E], monoid: Monoid[A]): F[A] = {
    chronicle.disclose(e)
  }

  def confess[E](e: E)(implicit chronicle: MonadChronicle[F, E]): F[A] = {
    chronicle.confess(e)
  }

  def materialize[E](implicit chronicle: MonadChronicle[F, E]): F[E Ior A] = {
    chronicle.materialize(fa)
  }

  def memento[E](implicit chronicle: MonadChronicle[F, E]): F[Either[E, A]] = {
    chronicle.memento(fa)
  }

  def absolve[E](a: => A)(implicit chronicle: MonadChronicle[F, E]): F[A] = {
    chronicle.absolve(a, fa)
  }

  def condemn[E](implicit chronicle: MonadChronicle[F, E]): F[A] = {
    chronicle.condemn(fa)
  }

  def retcon[E](cc: E => E)(implicit chronicle: MonadChronicle[F, E]): F[A] = {
    chronicle.retcon(cc, fa)
  }

  def chronicle[E](ior: E Ior A)(implicit chronicle: MonadChronicle[F, E]): F[A] = {
    chronicle.chronicle(ior)
  }
}

object chronicle extends ChronicleSyntax
