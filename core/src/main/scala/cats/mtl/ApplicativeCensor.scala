package cats
package mtl

import cats.data.{
  IndexedStateT,
  IndexedReaderWriterStateT,
  WriterT,
  Kleisli,
  StateT,
  OptionT,
  EitherT,
  IorT,
  ReaderWriterStateT => RWST
}
import cats.implicits._

trait ApplicativeCensor[F[_], L] extends FunctorListen[F, L] {
  val applicative: Applicative[F]
  val monoid: Monoid[L]
  override lazy val functor: Functor[F] = applicative

  def censor[A](fa: F[A])(f: L => L): F[A]

  def clear[A](fa: F[A]): F[A] = censor(fa)(_ => monoid.empty)
}

object ApplicativeCensor extends ApplicativeCensorInstances {
  def apply[F[_], L](implicit ev: ApplicativeCensor[F, L]): ApplicativeCensor[F, L] = ev
}

private[mtl] trait LowPriorityApplicativeCensorInstances {
  implicit final def inductiveApplicativeCensorWriterT[
      M[_]: Applicative,
      L0: Monoid,
      L: Monoid](implicit A: ApplicativeCensor[M, L]): ApplicativeCensor[WriterT[M, L0, *], L] =
    new FunctorListenInductiveWriterT[M, L0, L] with ApplicativeCensor[WriterT[M, L0, *], L] {
      val applicative: Applicative[WriterT[M, L0, *]] = WriterT.catsDataApplicativeForWriterT
      val monoid: Monoid[L] = Monoid[L]

      def censor[A](fa: WriterT[M, L0, A])(f: L => L): WriterT[M, L0, A] =
        WriterT(A.censor(fa.run)(f))
    }

  implicit final def inductiveApplicativeCensorRWST[M[_]: Monad, R, L0: Monoid, L: Monoid, S](
      implicit A: ApplicativeCensor[M, L]): ApplicativeCensor[RWST[M, R, L0, S, *], L] =
    new FunctorListenInductiveRWST[M, R, L0, L, S]
      with ApplicativeCensor[RWST[M, R, L0, S, *], L] {
      val applicative: Applicative[RWST[M, R, L0, S, *]] =
        IndexedReaderWriterStateT.catsDataMonadForRWST
      val monoid: Monoid[L] = Monoid[L]

      def censor[A](fa: RWST[M, R, L0, S, A])(f: L => L): RWST[M, R, L0, S, A] =
        RWST { case (r, s) => A.censor(fa.run(r, s))(f) }
    }
}

private[mtl] trait ApplicativeCensorInstances extends LowPriorityApplicativeCensorInstances {
  implicit final def applicativeCensorWriterT[M[_], L](
      implicit M: Applicative[M],
      L: Monoid[L]): ApplicativeCensor[WriterT[M, L, *], L] =
    new FunctorListenWriterT[M, L] with ApplicativeCensor[WriterT[M, L, *], L] {
      val applicative: Applicative[WriterT[M, L, *]] =
        cats.data.WriterT.catsDataApplicativeForWriterT[M, L]

      val monoid: Monoid[L] = L

      override def clear[A](fa: WriterT[M, L, A]): WriterT[M, L, A] =
        WriterT(fa.value.tupleLeft(L.empty))

      def censor[A](fa: WriterT[M, L, A])(f: L => L): WriterT[M, L, A] =
        WriterT(fa.run.map { case (l, a) => (f(l), a) })

    }

  implicit final def applicativeCensorRWST[M[_], R, L, S](
      implicit L: Monoid[L],
      M: Monad[M]): ApplicativeCensor[RWST[M, R, L, S, *], L] =
    new FunctorListenRWST[M, R, L, S] with ApplicativeCensor[RWST[M, R, L, S, *], L] {
      val applicative: Applicative[RWST[M, R, L, S, *]] =
        IndexedReaderWriterStateT.catsDataMonadForRWST

      val monoid: Monoid[L] = L

      def censor[A](faf: RWST[M, R, L, S, A])(f: L => L): RWST[M, R, L, S, A] =
        RWST((e, s) =>
          faf.run(e, s).map {
            case (l, s, a) => (f(l), s, a)
          })

    }

  implicit final def applicativeCensorKleisli[F[_], R, L](
      implicit L: Monoid[L],
      A: ApplicativeCensor[F, L],
      F: Applicative[F]): ApplicativeCensor[Kleisli[F, R, *], L] =
    new FunctorListenKleisli[F, R, L] with ApplicativeCensor[Kleisli[F, R, *], L] {
      val applicative: Applicative[Kleisli[F, R, *]] = Kleisli.catsDataApplicativeForKleisli
      val monoid: cats.Monoid[L] = L

      def censor[A](fa: Kleisli[F, R, A])(f: L => L): Kleisli[F, R, A] =
        Kleisli(r => A.censor(fa.run(r))(f))
    }

  implicit final def applicativeCensorStateT[F[_], S, L](
      implicit L: Monoid[L],
      A: ApplicativeCensor[F, L],
      F: Monad[F]): ApplicativeCensor[StateT[F, S, *], L] =
    new FunctorListenStateT[F, S, L] with ApplicativeCensor[StateT[F, S, *], L] {
      val applicative: Applicative[StateT[F, S, *]] =
        IndexedStateT.catsDataMonadForIndexedStateT
      val monoid: cats.Monoid[L] = L

      def censor[A](fa: StateT[F, S, A])(f: L => L): StateT[F, S, A] =
        StateT(s => A.censor(fa.run(s))(f))
    }

  implicit final def applicativeCensorEitherT[F[_], E, L](
      implicit L: Monoid[L],
      A: ApplicativeCensor[F, L],
      F: Monad[F]): ApplicativeCensor[EitherT[F, E, *], L] =
    new FunctorListenEitherT[F, E, L] with ApplicativeCensor[EitherT[F, E, *], L] {
      val applicative: Applicative[EitherT[F, E, *]] = EitherT.catsDataMonadErrorForEitherT
      val monoid: cats.Monoid[L] = L

      def censor[A](fa: EitherT[F, E, A])(f: L => L): EitherT[F, E, A] =
        EitherT(A.censor(fa.value)(f))
    }

  implicit final def applicativeCensorIorT[F[_], E, L](
      implicit L: Monoid[L],
      A: ApplicativeCensor[F, L],
      F: Monad[F],
      E: Semigroup[E]): ApplicativeCensor[IorT[F, E, *], L] =
    new FunctorListenIorT[F, E, L] with ApplicativeCensor[IorT[F, E, *], L] {
      val applicative: Applicative[IorT[F, E, *]] = IorT.catsDataMonadErrorForIorT
      val monoid: cats.Monoid[L] = L

      def censor[A](fa: IorT[F, E, A])(f: L => L): IorT[F, E, A] =
        IorT(A.censor(fa.value)(f))
    }

  implicit final def applicativeCensorOptionT[F[_], L](
      implicit L: Monoid[L],
      A: ApplicativeCensor[F, L],
      F: Monad[F]): ApplicativeCensor[OptionT[F, *], L] =
    new FunctorListenOptionT[F, L] with ApplicativeCensor[OptionT[F, *], L] {
      val applicative: Applicative[OptionT[F, *]] = OptionT.catsDataMonadForOptionT
      val monoid: cats.Monoid[L] = L

      def censor[A](fa: OptionT[F, A])(f: L => L): OptionT[F, A] =
        OptionT(A.censor(fa.value)(f))
    }

}
