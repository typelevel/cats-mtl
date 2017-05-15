package cats
package mtl
package instances

import cats.mtl.evidence.Nat
import cats.data._
import cats.implicits._

trait HandleInstances extends HandleInstancesLowPriority {

  implicit def handleNEither[M[_], E](implicit M: Monad[M]): Handle.Aux[Nat.Zero, CurryT[EitherTC[E]#l, M]#l, E] =
    new Handle[CurryT[EitherTC[E]#l, M]#l, E] {
      val raise: Raise.Aux[Nat.Zero, CurryT[EitherTC[E]#l, M]#l, E] =
        instances.raise.raiseNEither(M)

      type N = Nat.Zero

      def materialize[A](fa: EitherT[M, E, A]): EitherT[M, E, Either[E, A]] =
        EitherT(fa.value.map(_.asRight))

      def handleErrorWith[A](fa: EitherT[M, E, A])(f: PartialFunction[E, A]): EitherT[M, E, A] =
        fa.recover(f)
    }

}

trait HandleInstancesLowPriority {

  private type EitherC[E] = {type l[A] = E Either A}

  implicit def handleNIndEither[N0 <: Nat, M[_], E, Err](implicit under: Handle.Aux[N0, M, E]
                                                        ): Handle.Aux[Nat.Succ[N0], CurryT[EitherTC[Err]#l, M]#l, E] =
    new Handle[CurryT[EitherTC[Err]#l, M]#l, E] {
      val raise: Raise.Aux[Nat.Succ[N0], CurryT[EitherTC[Err]#l, M]#l, E] =
        instances.raise.raiseNInd[N0, EitherTC[Err]#l, M, E](
          EitherT.catsDataMonadErrorForEitherT(under.raise.monad),
          new TransLift[EitherTC[Err]#l] {
            type TC[F[_]] = Applicative[F]

            def liftT[F[_] : Applicative, A](ma: F[A]): EitherT[F, Err, A] =
              EitherT.liftT(ma)
          }, under.raise
        )

      type N = Nat.Succ[N0]

      implicit val myMonad: Monad[M] =
        under.raise.monad

      def materialize[A](fa: EitherT[M, Err, A]): EitherTC[Err]#l[M, Either[E, A]] = {
        EitherT[M, Err, E Either A](
          under.materialize(fa.value).map(e =>
            Traverse[EitherC[E]#l].sequence[EitherC[Err]#l, A](e)
          )
        )
      }

      def handleErrorWith[A](fa: EitherT[M, Err, A])(f: PartialFunction[E, A]): EitherT[M, Err, A] =
        EitherT(under.handleErrorWith(fa.value)(f.andThen(Right(_))))
    }

  implicit def handleNIndState[N0 <: Nat, M[_], E, Err](implicit under: Handle.Aux[N0, M, Err]
                                                       ): Handle.Aux[Nat.Succ[N0], CurryT[StateTC[E]#l, M]#l, Err] =
    new Handle[CurryT[StateTC[E]#l, M]#l, Err] {
      val raise: Raise.Aux[Nat.Succ[N0], CurryT[StateTC[E]#l, M]#l, Err] =
        instances.raise.raiseNInd[N0, StateTC[E]#l, M, Err](
          StateT.catsDataMonadForStateT(under.raise.monad),
          new TransLift[StateTC[E]#l] {
            type TC[F[_]] = Applicative[F]

            def liftT[F[_] : Applicative, A](ma: F[A]): StateT[F, E, A] =
              StateT.lift(ma)
          }, under.raise
        )

      type N = Nat.Succ[N0]

      def materialize[A](fa: StateT[M, E, A]): StateT[M, E, Err Either A] = {
        implicit val myMonad: Monad[M] =
          under.raise.monad
        StateT.applyF[M, E, Err Either A](fa.runF.map { d =>
          (e: E) =>
            val mea = d(e)
            under.materialize(mea).map[(E, Err Either A)] {
              case Right((newState, a)) => (newState, Right(a))
              case Left(err) => (e, Left(err))
            }
        })
      }

      def handleErrorWith[A](fa: StateT[M, E, A])(f: PartialFunction[Err, A]): StateT[M, E, A] =
        StateT((e: E) => under.handleErrorWith(fa.run(e))(f.andThen(a => (e, a))))
    }

  implicit def handleNIndReader[N0 <: Nat, M[_], Env, Err](implicit under: Handle.Aux[N0, M, Err]
                                                          ): Handle.Aux[Nat.Succ[N0], CurryT[ReaderTC[Env]#l, M]#l, Err] =
    new Handle[CurryT[ReaderTC[Env]#l, M]#l, Err] {
      val raise: Raise.Aux[Nat.Succ[N0], CurryT[ReaderTC[Env]#l, M]#l, Err] =
        instances.raise.raiseNInd[N0, ReaderTC[Env]#l, M, Err](
          Kleisli.catsDataMonadReaderForKleisli(under.raise.monad),
          new TransLift[ReaderTC[Env]#l] {
            type TC[F[_]] = Applicative[F]

            def liftT[F[_] : Applicative, A](ma: F[A]): ReaderT[F, Env, A] =
              ReaderT.lift(ma)
          }, under.raise
        )

      type N = Nat.Succ[N0]

      def materialize[A](fa: ReaderT[M, Env, A]): ReaderT[M, Env, Either[Err, A]] = {
        implicit val myMonad: Monad[M] =
          under.raise.monad
        ReaderT(fa.run.andThen(under.materialize))
      }

      def handleErrorWith[A](fa: ReaderT[M, Env, A])(f: PartialFunction[Err, A]): ReaderT[M, Env, A] =
        ReaderT((e: Env) => under.handleErrorWith(fa.run(e))(f))
    }

  implicit def handleNIndWriter[N0 <: Nat, M[_], L, Err](implicit L: Monoid[L], under: Handle.Aux[N0, M, Err]
                                                        ): Handle.Aux[Nat.Succ[N0], CurryT[WriterTC[L]#l, M]#l, Err] =
    new Handle[CurryT[WriterTC[L]#l, M]#l, Err] {
      val raise: Raise.Aux[Nat.Succ[N0], CurryT[WriterTC[L]#l, M]#l, Err] =
        instances.raise.raiseNInd[N0, WriterTC[L]#l, M, Err](
          WriterT.catsDataMonadWriterForWriterT[M, L](under.raise.monad, L),
          new TransLift[WriterTC[L]#l] {
            type TC[F[_]] = Applicative[F]

            def liftT[F[_] : Applicative, A](ma: F[A]): WriterT[F, L, A] =
              WriterT.lift(ma)
          }, under.raise
        )

      type N = Nat.Succ[N0]

      def materialize[A](fa: WriterT[M, L, A]): WriterT[M, L, Either[Err, A]] = {
        implicit val myMonad: Monad[M] =
          under.raise.monad
        WriterT(under.materialize(fa.run).map {
          case Left(err) => (L.empty, Left(err))
          case Right((e, a)) => (e, Right(a))
        })
      }

      def handleErrorWith[A](fa: WriterT[M, L, A])(f: PartialFunction[Err, A]): WriterT[M, L, A] =
        WriterT(under.handleErrorWith(fa.run)(f.andThen(a => (L.empty, a))))
    }

}

object handle extends HandleInstances
