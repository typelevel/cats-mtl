package cats
package mtl
package instances

import cats.data._
import cats.implicits._
import cats.mtl.FunctorRaise

trait HandleInstances extends HandleInstancesLowPriority {
  implicit final def handleEitherT[M[_], E](implicit M: Monad[M]): ApplicativeHandle[EitherTC[M, E]#l, E] = {
    new ApplicativeHandle[EitherTC[M, E]#l, E] {
      val applicative: Applicative[EitherTC[M, E]#l] = new Applicative[EitherTC[M, E]#l] {
        def pure[A](x: A): EitherT[M, E, A] = EitherT.pure(x)

        def ap[A, B](ff: EitherT[M, E, (A) => B])(fa: EitherT[M, E, A]): EitherT[M, E, B] =
          EitherT((ff.value |@| fa.value).map((fe, ae) => fe.flatMap(f => ae.map(f))))
      }
      val raise: FunctorRaise[EitherTC[M, E]#l, E] =
        instances.raise.raiseEitherT(M)

      def attempt[A](fa: EitherT[M, E, A]): EitherT[M, E, Either[E, A]] = {
        EitherT(fa.value.map(_.asRight))
      }

      def recover[A](fa: EitherT[M, E, A])(f: PartialFunction[E, A]): EitherT[M, E, A] = {
        fa.recover(f)
      }

      def recoverWith[A](fa: EitherT[M, E, A])(f: PartialFunction[E, EitherT[M, E, A]]): EitherT[M, E, A] = {
        fa.recoverWith(f)
      }

      def handleErrorWith[A](fa: EitherT[M, E, A])(f: (E) => EitherT[M, E, A]): EitherT[M, E, A] = {
        fa.recoverWith(PartialFunction(f))
      }

      def handleError[A](fa: EitherT[M, E, A])(f: (E) => A): EitherT[M, E, A] = {
        fa.recover(PartialFunction(f))
      }
    }
  }
}

private[instances] trait HandleInstancesLowPriority {
  implicit final def handleEitherTInd[M[_], E, Err](implicit under: ApplicativeHandle[M, E],
                                                    M: Applicative[M]
                                                   ): ApplicativeHandle[CurryT[EitherTCE[Err]#l, M]#l, E] = {
    new ApplicativeHandle[EitherTC[M, Err]#l, E] {
      val applicative: Applicative[EitherTC[M, Err]#l] = new Applicative[EitherTC[M, Err]#l] {
        def pure[A](x: A): EitherT[M, Err, A] = EitherT.pure(x)

        def ap[A, B](ff: EitherT[M, Err, (A) => B])(fa: EitherT[M, Err, A]): EitherT[M, Err, B] =
          EitherT((ff.value |@| fa.value).map((fe, ae) => fe.flatMap(f => ae.map(f))))
      }
      val raise: FunctorRaise[CurryT[EitherTCE[Err]#l, M]#l, E] =
        instances.raise.raiseInd[EitherTC[M, Err]#l, M, E](
          instances.eithert.eitherFunctorLayerFunctor[M, Err],
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

      def handleErrorWith[A](fa: EitherT[M, Err, A])(f: (E) => EitherT[M, Err, A]): EitherT[M, Err, A] = {
        EitherT(under.handleErrorWith(fa.value)(f.andThen(_.value)))
      }

      def handleError[A](fa: EitherT[M, Err, A])(f: (E) => A): EitherT[M, Err, A] = {
        EitherT(under.handleError(fa.value)(f.andThen(Right(_))))
      }
    }
  }

  implicit final def handleStateTInd[M[_], S, Err](implicit under: ApplicativeHandle[M, Err],
                                                   M: Monad[M]
                                                  ): ApplicativeHandle[CurryT[StateTCS[S]#l, M]#l, Err] = {
    new ApplicativeHandle[StateTC[M, S]#l, Err] {
      val applicative: Applicative[StateTC[M, S]#l] = StateT.catsDataMonadForStateT(M)
      val raise: FunctorRaise[CurryT[StateTCS[S]#l, M]#l, Err] =
        instances.raise.raiseInd[StateTC[M, S]#l, M, Err](
          instances.statet.stateMonadLayerControl[M, S],
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

      def handleErrorWith[A](fa: StateT[M, S, A])(f: (Err) => StateT[M, S, A]): StateT[M, S, A] = {
        StateT((e: S) => under.recoverWith(fa.run(e))(PartialFunction(f.andThen(_.run(e)))))
      }

      def handleError[A](fa: StateT[M, S, A])(f: (Err) => A): StateT[M, S, A] = {
        StateT((e: S) => under.recover(fa.run(e))(PartialFunction(f.andThen(a => (e, a)))))
      }
    }
  }

  implicit final def handleReaderTInd[M[_], Env, Err](implicit under: ApplicativeHandle[M, Err],
                                                      M: Applicative[M]
                                                     ): ApplicativeHandle[CurryT[ReaderTCE[Env]#l, M]#l, Err] = {
    new ApplicativeHandle[ReaderTC[M, Env]#l, Err] {
      val applicative: Applicative[ReaderTC[M, Env]#l] = ReaderT.catsDataApplicativeForKleisli(M)
      val raise: FunctorRaise[CurryT[ReaderTCE[Env]#l, M]#l, Err] =
        instances.raise.raiseInd[ReaderTC[M, Env]#l, M, Err](
          instances.readert.readerApplicativeLayerFunctor[M, Env],
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

      def handleErrorWith[A](fa: ReaderT[M, Env, A])(f: (Err) => ReaderT[M, Env, A]): ReaderT[M, Env, A] = {
        ReaderT((e: Env) => under.handleErrorWith(fa.run(e))(f(_).run(e)))
      }

      def handleError[A](fa: ReaderT[M, Env, A])(f: (Err) => A): ReaderT[M, Env, A] = {
        fa.mapF(under.handleError(_)(f))
      }
    }
  }

  implicit final def handleWriterTInd[M[_], L, Err](implicit L: Monoid[L],
                                                    under: ApplicativeHandle[M, Err],
                                                    M: Applicative[M]
                                                   ): ApplicativeHandle[CurryT[WriterTCL[L]#l, M]#l, Err] = {
    new ApplicativeHandle[WriterTC[M, L]#l, Err] {
      val applicative: Applicative[WriterTC[M, L]#l] = WriterT.catsDataApplicativeForWriterT(M, L)
      val raise: FunctorRaise[CurryT[WriterTCL[L]#l, M]#l, Err] =
        instances.raise.raiseInd[WriterTC[M, L]#l, M, Err](
          instances.writert.writerApplicativeLayerFunctor[M, L],
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

      def handleErrorWith[A](fa: WriterT[M, L, A])(f: (Err) => WriterT[M, L, A]): WriterT[M, L, A] = {
        WriterT(under.handleErrorWith(fa.run)(f.andThen(_.run)))
      }

      def handleError[A](fa: WriterT[M, L, A])(f: (Err) => A): WriterT[M, L, A] = {
        WriterT(under.handleError(fa.run)(f.andThen(a => (L.empty, a))))
      }
    }
  }
}

object handle extends HandleInstances
