package cats
package mtl

import cats.data.Ior

trait MonadChronicle[F[_], E] extends Serializable {
  val monad: Monad[F]

  def dictate(c: E): F[Unit]

  def disclose[A](c: E)(implicit M: Monoid[A]): F[A]

  def confess[A](c: E): F[A]

  def memento[A](ma: F[A]): F[Either[E, A]]

  def absolve[A](a: A, ma: F[A]): F[A]

  def condemn[A](ma: F[A]): F[A]

  def retcon[A](cc: E => E, ma: F[A]): F[A]

  def chronicle[A](ior: E Ior A): F[A]
}

object MonadChronicle {
  def apply[F[_], E](implicit monadChronicle: MonadChronicle[F, E]): MonadChronicle[F, E] =
    monadChronicle
}
