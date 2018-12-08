package cats
package mtl

import cats.data.Ior

trait MonadChronicle[F[_], E] extends Serializable {
  val monad: Monad[F]

  def dictate(c: E): F[Unit]

  def disclose[A](c: E)(implicit M: Monoid[A]): F[A]

  def confess[A](c: E): F[A]

  def materialize[A](fa: F[A]): F[E Ior A]

  def memento[A](fa: F[A]): F[Either[E, A]]

  def absolve[A](fa: F[A])(a: => A): F[A]

  def condemn[A](fa: F[A]): F[A]

  def retcon[A](fa: F[A])(cc: E => E): F[A]

  def chronicle[A](ior: E Ior A): F[A]
}

object MonadChronicle {
  def dictate[F[_], E](e: E)(implicit ev: MonadChronicle[F, E]): F[Unit] = ev.dictate(e)

  def disclose[F[_], A, E](c: E)(implicit ev: MonadChronicle[F, E], m: Monoid[A]): F[A] =
    ev.disclose(c)

  def confess[F[_], E, A](c: E)(implicit ev: MonadChronicle[F, E]): F[A] = ev.confess(c)

  def materialize[F[_], E, A](fa: F[A])(implicit ev: MonadChronicle[F, E]): F[E Ior A] =
    ev.materialize(fa)

  def chronicle[F[_], E, A](ior: E Ior A)(implicit ev: MonadChronicle[F, E]): F[A] =
    ev.chronicle(ior)

  def apply[F[_], E](implicit ev: MonadChronicle[F, E]): MonadChronicle[F, E] =
    ev
}

trait DefaultMonadChronicle[F[_], E] extends MonadChronicle[F, E] {
  override def disclose[A](c: E)(implicit M: Monoid[A]): F[A] = monad.as(dictate(c), M.empty)

  override def memento[A](fa: F[A]): F[Either[E, A]] = monad.flatMap(materialize(fa)) {
    case Ior.Left(e)    => monad.pure(Left(e))
    case Ior.Right(a)   => monad.pure(Right(a))
    case Ior.Both(e, a) => monad.as(dictate(e), Right(a))
  }

  override def absolve[A](fa: F[A])(a: => A): F[A] = monad.map(materialize(fa)) {
    case Ior.Left(_)     => a
    case Ior.Right(a0)   => a0
    case Ior.Both(_, a0) => a0
  }

  override def condemn[A](fa: F[A]): F[A] = monad.flatMap(materialize(fa)) {
    case Ior.Left(e)    => confess(e)
    case Ior.Right(a)   => monad.pure(a)
    case Ior.Both(e, _) => confess(e)
  }

  override def retcon[A](fa: F[A])(cc: E => E): F[A] = monad.flatMap(materialize(fa)) {
    case Ior.Left(e)    => confess(cc(e))
    case Ior.Right(a)   => monad.pure(a)
    case Ior.Both(e, a) => monad.as(dictate(cc(e)), a)
  }

  override def chronicle[A](ior: E Ior A): F[A] = ior match {
    case Ior.Left(e)    => confess(e)
    case Ior.Right(a)   => monad.pure(a)
    case Ior.Both(e, a) => monad.as(dictate(e), a)
  }
}
