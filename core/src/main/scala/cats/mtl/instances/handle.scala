package cats
package mtl
package instances

import cats.mtl.evidence.Nat
import cats.data._
import cats.implicits._

trait HandleInstances extends HandleInstancesLowPriority {

  implicit def handleNEither[M[_], E](implicit M: Monad[M]): Handle.Aux[Nat.Zero, CurryT[EitherTC[E]#l, M]#l, E] =
    new Handle[CurryT[EitherTC[E]#l, M]#l, E] {
      val raise: RaiseN[Nat.Zero, CurryT[EitherTC[E]#l, M]#l, E] =
        RaiseN.raiseNEither(M)

      type N = Nat.Zero

      def materialize[A](fa: EitherT[M, E, A]): EitherT[M, E, Either[E, A]] =
        EitherT(fa.value.map(_.asRight))
    }

}

trait HandleInstancesLowPriority {

  private type EitherC[E] = {type l[A] = E Either A}

  implicit def handleNIndEither[N <: Nat, M[_], E, Err](implicit under: Handle[N, M, E]
                                                       ): Handle[Nat.Succ[N], CurryT[EitherTC[Err]#l, M]#l, E] =
    new Handle[Nat.Succ[N], CurryT[EitherTC[Err]#l, M]#l, E] {
      val raise: RaiseN[Nat.Succ[N], CurryT[EitherTC[Err]#l, M]#l, E] =
        RaiseN.raiseNInd[N, EitherTC[Err]#l, M, E](
          EitherT.catsDataMonadErrorForEitherT(under.raise.monad),
          new TransLift[EitherTC[Err]#l] {
            type TC[F[_]] = Applicative[F]

            def liftT[F[_] : Applicative, A](ma: F[A]): EitherT[F, Err, A] =
              EitherT.liftT(ma)
          }, under.raise
        )

      def materialize[A](fa: EitherT[M, Err, A]): EitherTC[Err]#l[M, Either[E, A]] = {
        implicit val myMonad: Monad[M] =
          under.raise.monad
        EitherT[M, Err, E Either A](
          under.materialize(fa.value).map(e =>
            Traverse[EitherC[E]#l].sequence[EitherC[Err]#l, A](e)
          )
        )
      }
    }

  implicit def handleNIndState[N <: Nat, M[_], E, Err](implicit under: Handle[N, M, Err]
                                                      ): Handle[Nat.Succ[N], CurryT[StateTC[E]#l, M]#l, Err] =
    new Handle[Nat.Succ[N], CurryT[StateTC[E]#l, M]#l, Err] {
      val raise: RaiseN[Nat.Succ[N], CurryT[StateTC[E]#l, M]#l, Err] =
        RaiseN.raiseNInd[N, StateTC[E]#l, M, Err](
          StateT.catsDataMonadForStateT(under.raise.monad),
          new TransLift[StateTC[E]#l] {
            type TC[F[_]] = Applicative[F]

            def liftT[F[_] : Applicative, A](ma: F[A]): StateT[F, E, A] =
              StateT.lift(ma)
          }, under.raise
        )

      type StateC[E] = {type l[A] = E State A}
      type PairC[E] = {type l[A] = (E, A)}

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
    }

  implicit def handleNIndReader[N <: Nat, M[_], Env, Err](implicit under: Handle[N, M, Err]
                                                       ): Handle[Nat.Succ[N], CurryT[ReaderTC[Env]#l, M]#l, Err] =
    new Handle[Nat.Succ[N], CurryT[ReaderTC[Env]#l, M]#l, Err] {
      val raise: RaiseN[Nat.Succ[N], CurryT[ReaderTC[Env]#l, M]#l, Err] =
        RaiseN.raiseNInd[N, ReaderTC[Env]#l, M, Err](
          Kleisli.catsDataMonadReaderForKleisli(under.raise.monad),
          new TransLift[ReaderTC[Env]#l] {
            type TC[F[_]] = Applicative[F]

            def liftT[F[_] : Applicative, A](ma: F[A]): ReaderT[F, Env, A] =
              ReaderT.lift(ma)
          }, under.raise
        )

      def materialize[A](fa: ReaderT[M, Env, A]): ReaderT[M, Env, Either[Err, A]] = {
        implicit val myMonad: Monad[M] =
          under.raise.monad
        ReaderT(fa.run.andThen(under.materialize))
      }
    }

  implicit def handleNIndWriter[N <: Nat, M[_], L, Err](implicit L: Monoid[L], under: Handle[N, M, Err]
                                                       ): Handle[Nat.Succ[N], CurryT[WriterTC[L]#l, M]#l, Err] =
    new Handle[Nat.Succ[N], CurryT[WriterTC[L]#l, M]#l, Err] {
      val raise: RaiseN[Nat.Succ[N], CurryT[WriterTC[L]#l, M]#l, Err] =
        RaiseN.raiseNInd[N, WriterTC[L]#l, M, Err](
          WriterT.catsDataMonadWriterForWriterT[M, L](under.raise.monad, L),
          new TransLift[WriterTC[L]#l] {
            type TC[F[_]] = Applicative[F]

            def liftT[F[_] : Applicative, A](ma: F[A]): WriterT[F, L, A] =
              WriterT.lift(ma)
          }, under.raise
        )

      def materialize[A](fa: WriterT[M, L, A]): WriterT[M, L, Either[Err, A]] = {
        implicit val myMonad: Monad[M] =
          under.raise.monad
        val x: M[Either[Err, (L, A)]] = under.materialize(fa.run)
        val y: M[(L, Either[Err, A])] = x.map {
          case Left(err) => (L.empty, Left(err))
          case Right((e, a)) => (e, Right(a))
        }
        WriterT(y)
      }
    }

}

object handle extends HandleInstances
