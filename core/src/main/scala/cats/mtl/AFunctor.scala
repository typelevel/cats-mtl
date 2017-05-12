package cats
package mtl

import cats.data._
import cats.syntax.all._

trait AFunctor[T[_[_], _]] extends TransLift[T] {
  type TC[F[_]] = Applicative[F]

  def hoist[F[_]: Functor, G[_], A](input: T[F, A])(trans: F ~> G): T[G, A]
}


object AFunctor {
  implicit def eitherTMMonad[E]: AFunctor[EitherTC[E]#l] =
    new AFunctor[EitherTC[E]#l] {
      def hoist[F[_]: Functor, G[_], A](input: EitherT[F, E, A])(trans: F ~> G): EitherT[G, E, A] =
        EitherT(trans(input.value))

      def liftT[M[_]: Applicative, A](ma: M[A]): EitherT[M, E, A] =
        EitherT.liftT(ma)
    }

  implicit def stateTMMonad[S]: AFunctor[StateTC[S]#l] =
    new AFunctor[StateTC[S]#l] {
      def hoist[F[_]: Functor, G[_], A](input: StateT[F, S, A])(trans: F ~> G): StateT[G, S, A] =
        StateT.applyF[G, S, A](trans(input.runF.map(_.andThen(trans(_)))))

      def liftT[M[_]: Applicative, A](ma: M[A]): StateT[M, S, A] =
        StateT.lift(ma)
    }

  implicit def readerTMMonad[E]: AFunctor[ReaderTC[E]#l] =
    new AFunctor[ReaderTC[E]#l] {
      def hoist[F[_]: Functor, G[_], A](input: ReaderT[F, E, A])(trans: F ~> G): ReaderT[G, E, A] =
        ReaderT[G, E, A](input.run.andThen(trans(_)))

      def liftT[M[_]: Applicative, A](ma: M[A]): ReaderT[M, E, A] =
        ReaderT.lift(ma)
    }

  implicit def writerTMMonad[L: Monoid]: AFunctor[WriterTC[L]#l] =
    new AFunctor[WriterTC[L]#l] {
      def hoist[F[_]: Functor, G[_], A](input: WriterT[F, L, A])(trans: F ~> G): WriterT[G, L, A] =
        WriterT[G, L, A](trans(input.run))

      def liftT[M[_]: Applicative, A](ma: M[A]): WriterT[M, L, A] =
        WriterT.lift[M, L, A](ma)
    }

}