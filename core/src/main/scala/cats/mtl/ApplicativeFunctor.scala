package cats
package mtl

import cats.data._
import cats.syntax.cartesian._

trait ApplicativeFunctor[T[_[_], _]] extends Serializable {
  def instanceA[F[_]: Applicative]: Applicative[CurryT[T, F]#l]
  def mapTS[F[_]: Applicative, G[_], A](tfa: T[F, A])(trans: F ~> G): T[G, A]
  def mapTT[F[_], G[_]: Applicative, A](tfa: T[F, A])(trans: F ~> G): T[G, A]
}

object ApplicativeFunctor {

  implicit def eithertTFunctor[E]: ApplicativeFunctor[EitherTCE[E]#l] = {
    new ApplicativeFunctor[EitherTCE[E]#l] {
      def mapTS[F[_]: Applicative, G[_], A](tfa: EitherT[F, E, A])(trans: F ~> G): EitherT[G, E, A] = EitherT(trans(tfa.value))
      def mapTT[F[_], G[_]: Applicative, A](tfa: EitherT[F, E, A])(trans: F ~> G): EitherT[G, E, A] = EitherT(trans(tfa.value))

      def instanceA[F[_] : Applicative]: Applicative[EitherTC[F, E]#l] = {
        new Applicative[EitherTC[F, E]#l] {
          def pure[A](x: A): EitherT[F, E, A] = EitherT.pure(x)

          def ap[A, B](ff: EitherT[F, E, (A) => B])(fa: EitherT[F, E, A]): EitherT[F, E, B] = {
            EitherT((ff.value |@| fa.value).map { (f, a) =>
              for {
                fr <- f.right
                ar <- a.right
              } yield fr(ar)
            })
          }
        }
      }
    }
  }

  implicit def readertTFunctor[E]: ApplicativeFunctor[ReaderTCE[E]#l] = {
    new ApplicativeFunctor[ReaderTCE[E]#l] {
      def mapTS[F[_]: Applicative, G[_], A](tfa: ReaderT[F, E, A])(trans: F ~> G): ReaderT[G, E, A] = tfa.transform(trans)
      def mapTT[F[_], G[_]: Applicative, A](tfa: ReaderT[F, E, A])(trans: F ~> G): ReaderT[G, E, A] = tfa.transform(trans)

      def instanceA[F[_] : Applicative]: Applicative[ReaderTC[F, E]#l] = ReaderT.catsDataApplicativeForKleisli
    }
  }

  implicit def writertTFunctor[L: Monoid]: ApplicativeFunctor[WriterTCL[L]#l] = {
    new ApplicativeFunctor[WriterTCL[L]#l] {
      def mapTS[F[_]: Applicative, G[_], A](tfa: WriterT[F, L, A])(trans: F ~> G): WriterT[G, L, A] = WriterT(trans(tfa.run))
      def mapTT[F[_], G[_]: Applicative, A](tfa: WriterT[F, L, A])(trans: F ~> G): WriterT[G, L, A] = WriterT(trans(tfa.run))

      def instanceA[F[_] : Applicative]: Applicative[WriterTC[F, L]#l] = WriterT.catsDataApplicativeForWriterT[F, L]
    }
  }

  implicit def optiontTFunctor[E]: ApplicativeFunctor[OptionT] = {
    new ApplicativeFunctor[OptionT] {
      def mapTS[F[_]: Applicative, G[_], A](tfa: OptionT[F, A])(trans: F ~> G): OptionT[G, A] = OptionT(trans(tfa.value))
      def mapTT[F[_], G[_]: Applicative, A](tfa: OptionT[F, A])(trans: F ~> G): OptionT[G, A] = OptionT(trans(tfa.value))

      def instanceA[F[_] : Applicative]: Applicative[OptionTC[F]#l] = {
        new Applicative[OptionTC[F]#l] {
          def pure[A](x: A): OptionT[F, A] = OptionT.pure(x)

          def ap[A, B](ff: OptionT[F, (A) => B])(fa: OptionT[F, A]): OptionT[F, B] = {
            OptionT((ff.value |@| fa.value).map { (f, a) =>
              for {
                fo <- f
                ao <- a
              } yield fo(ao)
            })
          }
        }
      }
    }
  }
}
