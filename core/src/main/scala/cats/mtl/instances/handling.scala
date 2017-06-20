package cats
package mtl
package instances

import cats.data._
import cats.implicits._
import cats.mtl.functor.Raising

trait HandlingInstances extends HandlingInstancesLowPriority {
  implicit final def handleNEither[M[_], E](implicit M: Monad[M]): monad.Handling[EitherTC[M, E]#l, E] = {
    new monad.Handling[EitherTC[M, E]#l, E] {
      val monad = EitherT.catsDataMonadErrorForEitherT(M)
      val raise: Raising[EitherTC[M, E]#l, E] =
        instances.raising.raiseNEither(M)

      def attempt[A](fa: EitherT[M, E, A]): EitherT[M, E, Either[E, A]] = {
        EitherT(fa.value.map(_.asRight))
      }

      def recover[A](fa: EitherT[M, E, A])(f: PartialFunction[E, A]): EitherT[M, E, A] = {
        fa.recover(f)
      }

      def recoverWith[A](fa: EitherT[M, E, A])(f: PartialFunction[E, EitherT[M, E, A]]): EitherT[M, E, A] = {
        fa.recoverWith(f)
      }
    }
  }
}

private[instances] trait HandlingInstancesLowPriority {
  implicit final def handleNIndEither[M[_], E, Err](implicit under: monad.Handling[M, E],
                                                    M: Monad[M]
                                                   ): monad.Handling[CurryT[EitherTCE[Err]#l, M]#l, E] = {
    new monad.Handling[CurryT[EitherTCE[Err]#l, M]#l, E] {
      val monad = EitherT.catsDataMonadErrorForEitherT(M)
      val raise: Raising[CurryT[EitherTCE[Err]#l, M]#l, E] =
        instances.raising.raiseNInd[EitherTC[M, Err]#l, M, E](
          instances.eithert.eitherMonadTransFunctor[M, Err],
          under.raise
        )

      def attempt[A](fa: EitherT[M, Err, A]): EitherTCE[Err]#l[M, Either[E, A]] = {
        EitherT[M, Err, E Either A](
          under.attempt(fa.value).map(e =>
            Traverse[EitherC[E]#l].sequence[EitherC[Err]#l, A](e)
          )
        )
      }

      def recover[A](fa: EitherT[M, Err, A])(f: PartialFunction[E, A]): EitherT[M, Err, A] = {
        EitherT(under.recover(fa.value)(f.andThen(Right(_))))
      }

      def recoverWith[A](fa: EitherT[M, Err, A])(f: PartialFunction[E, EitherT[M, Err, A]]): EitherT[M, Err, A] = {
        EitherT(under.recoverWith(fa.value)(f.andThen(_.value)))
      }
    }
  }

  implicit final def handleNIndState[M[_], S, Err](implicit under: monad.Handling[M, Err],
                                                   M: Monad[M]
                                                  ): monad.Handling[CurryT[StateTCS[S]#l, M]#l, Err] = {
    new monad.Handling[CurryT[StateTCS[S]#l, M]#l, Err] {
      val monad = StateT.catsDataMonadForStateT(M)
      val raise: Raising[CurryT[StateTCS[S]#l, M]#l, Err] =
        instances.raising.raiseNInd[StateTC[M, S]#l, M, Err](
          instances.statet.stateMonadLayer[M, S],
          under.raise
        )

      def attempt[A](fa: StateT[M, S, A]): StateT[M, S, Err Either A] = {
        StateT.applyF[M, S, Err Either A](fa.runF.map { d =>
          (e: S) =>
            under.attempt(d(e)).map[(S, Err Either A)] {
              case Right((newState, a)) => (newState, Right(a))
              case Left(err) => (e, Left(err))
            }
        })
      }

      def recover[A](fa: StateT[M, S, A])(f: PartialFunction[Err, A]): StateT[M, S, A] = {
        StateT((e: S) => under.recover(fa.run(e))(f.andThen(a => (e, a))))
      }

      def recoverWith[A](fa: StateT[M, S, A])(f: PartialFunction[Err, StateT[M, S, A]]): StateT[M, S, A] = {
        StateT((e: S) => under.recoverWith(fa.run(e))(f.andThen(_.run(e))))
      }
    }
  }

  implicit final def handleNIndReader[M[_], Env, Err](implicit under: monad.Handling[M, Err],
                                                      M: Monad[M]
                                                     ): monad.Handling[CurryT[ReaderTCE[Env]#l, M]#l, Err] = {
    new monad.Handling[CurryT[ReaderTCE[Env]#l, M]#l, Err] {
      val monad = ReaderT.catsDataMonadReaderForKleisli(M)
      val raise: Raising[CurryT[ReaderTCE[Env]#l, M]#l, Err] =
        instances.raising.raiseNInd[ReaderTC[M, Env]#l, M, Err](
          instances.readert.readerMonadLayer[M, Env],
          under.raise
        )

      def attempt[A](fa: ReaderT[M, Env, A]): ReaderT[M, Env, Either[Err, A]] = {
        ReaderT(fa.run.andThen(under.attempt))
      }

      def recover[A](fa: ReaderT[M, Env, A])
                    (f: PartialFunction[Err, A]): ReaderT[M, Env, A] = {
        ReaderT((e: Env) => under.recover(fa.run(e))(f))
      }

      def recoverWith[A](fa: ReaderT[M, Env, A])
                        (f: PartialFunction[Err, ReaderT[M, Env, A]]): ReaderT[M, Env, A] = {
        ReaderT((e: Env) => under.recoverWith(fa.run(e))(f.andThen(_.run(e))))
      }
    }
  }

  implicit final def handleNIndWriter[M[_], L, Err](implicit L: Monoid[L],
                                                    under: monad.Handling[M, Err],
                                                    M: Monad[M]
                                                   ): monad.Handling[CurryT[WriterTCL[L]#l, M]#l, Err] = {
    new monad.Handling[CurryT[WriterTCL[L]#l, M]#l, Err] {
      val monad = WriterT.catsDataMonadWriterForWriterT(M, L)
      val raise: Raising[CurryT[WriterTCL[L]#l, M]#l, Err] =
        instances.raising.raiseNInd[WriterTC[M, L]#l, M, Err](
          instances.writert.writerMonadLayer[M, L],
          under.raise
        )

      def attempt[A](fa: WriterT[M, L, A]): WriterT[M, L, Either[Err, A]] = {
        WriterT(under.attempt(fa.run).map {
          case Left(err) => (L.empty, Left(err))
          case Right((e, a)) => (e, Right(a))
        })
      }

      def recover[A](fa: WriterT[M, L, A])(f: PartialFunction[Err, A]): WriterT[M, L, A] = {
        WriterT(under.recover(fa.run)(f.andThen(a => (L.empty, a))))
      }

      def recoverWith[A](fa: WriterT[M, L, A])
                        (f: PartialFunction[Err, WriterT[M, L, A]]): WriterT[M, L, A] = {
        WriterT(under.recoverWith(fa.run)(f.andThen(_.run)))
      }
    }
  }
}

object handling extends HandlingInstances
