package cats
package mtl
package monad

import cats.data.{EitherT, OptionT, ReaderT, WriterT}
import cats.syntax.all._

trait TFunctor[T[_[_], _]] extends applicative.TFunctor[T] {
  def instanceM[F[_] : Monad]: Monad[CurryT[T, F]#l]

  def mapT[F[_], G[_], A](tfa: T[F, A])(trans: F ~> G): T[G, A]
}

object TFunctor {
  implicit def eithertTFunctor[E]: TFunctor[EitherTCE[E]#l] = {
    new TFunctor[EitherTCE[E]#l] {
      def mapT[F[_], G[_], A](tfa: EitherT[F, E, A])(trans: F ~> G): EitherT[G, E, A] = EitherT(trans(tfa.value))

      def instanceM[F[_] : Monad]: Monad[EitherTC[F, E]#l] = EitherT.catsDataMonadErrorForEitherT

      def instanceA[F[_] : Applicative]: Applicative[EitherTC[F, E]#l] = {
        new Applicative[EitherTC[F, E]#l] {
          def pure[A](x: A): EitherT[F, E, A] = EitherT.pure(x)

          def ap[A, B](ff: EitherT[F, E, (A) => B])(fa: EitherT[F, E, A]): EitherT[F, E, B] = {
            EitherT((ff.value |@| fa.value).map { (f, a) =>
              for {
                fr <- f
                ar <- a
              } yield fr(ar)
            })
          }
        }
      }

      def instanceF[F[_] : Functor]: Functor[EitherTC[F, E]#l] = EitherT.catsDataFunctorForEitherT
    }
  }

  implicit def readertTFunctor[E]: TFunctor[ReaderTCE[E]#l] = {
    new TFunctor[ReaderTCE[E]#l] {
      def mapT[F[_], G[_], A](tfa: ReaderT[F, E, A])(trans: F ~> G): ReaderT[G, E, A] = tfa.transform(trans)

      def instanceM[F[_] : Monad]: Monad[ReaderTC[F, E]#l] = ReaderT.catsDataMonadReaderForKleisli

      def instanceA[F[_] : Applicative]: Applicative[ReaderTC[F, E]#l] = ReaderT.catsDataApplicativeForKleisli

      def instanceF[F[_] : Functor]: Functor[ReaderTC[F, E]#l] = ReaderT.catsDataFunctorForKleisli
    }
  }

  implicit def writertTFunctor[L: Monoid]: TFunctor[WriterTCL[L]#l] = {
    new TFunctor[WriterTCL[L]#l] {
      def mapT[F[_], G[_], A](tfa: WriterT[F, L, A])(trans: F ~> G): WriterT[G, L, A] = WriterT(trans(tfa.run))

      def instanceM[F[_] : Monad]: Monad[WriterTC[F, L]#l] = WriterT.catsDataMonadWriterForWriterT[F, L]

      def instanceA[F[_] : Applicative]: Applicative[WriterTC[F, L]#l] = WriterT.catsDataApplicativeForWriterT[F, L]

      def instanceF[F[_] : Functor]: Functor[WriterTC[F, L]#l] = new Functor[WriterTC[F, L]#l] {
        def map[A, B](fa: WriterT[F, L, A])(f: (A) => B): WriterT[F, L, B] = fa.map(f)
      }
    }
  }

  implicit def optiontTFunctor[E]: TFunctor[OptionT] = {
    new TFunctor[OptionT] {
      def mapT[F[_], G[_], A](tfa: OptionT[F, A])(trans: F ~> G): OptionT[G, A] = OptionT(trans(tfa.value))

      def instanceM[F[_] : Monad]: Monad[OptionTC[F]#l] = OptionT.catsDataMonadForOptionT

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

      def instanceF[F[_] : Functor]: Functor[OptionTC[F]#l] = OptionT.catsDataFunctorFilterForOptionT
    }
  }
}
