package cats
package mtl

trait ApplicativeCensor[F[_], L] extends FunctorListen[F, L] {
  val applicative: Applicative[F]
  val monoid: Monoid[L]
  override lazy val functor: Functor[F] = applicative

  def censor[A](fa: F[A])(f: L => L): F[A]

  def clear[A](fa: F[A]): F[A]
}

object ApplicativeCensor {
  def apply[F[_], L](implicit ev: ApplicativeCensor[F, L]): ApplicativeCensor[F, L] = ev
}

trait DefaultApplicativeCensor[F[_], L] extends ApplicativeCensor[F, L] with DefaultFunctorListen[F, L] {
  override def clear[A](fa: F[A]): F[A] =
    censor(fa)(_ => monoid.empty)
}
