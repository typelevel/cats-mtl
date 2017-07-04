package cats
package mtl

import cats.data._
import cats.syntax.functor._

trait TFunctor[T[_[_], _]] extends Serializable {
  def mapTS[F[_] : Functor, G[_], A](tfa: T[F, A])(trans: F ~> G): T[G, A]

  def mapTT[F[_], G[_] : Functor, A](tfa: T[F, A])(trans: F ~> G): T[G, A]
}

object TFunctor {

  implicit def eithertTFunctor[E]: TFunctor[EitherTCE[E]#l] = {
    new TFunctor[EitherTCE[E]#l] {
      def mapTS[F[_] : Functor, G[_], A](tfa: EitherT[F, E, A])(trans: F ~> G): EitherT[G, E, A] = EitherT(trans(tfa.value))

      def mapTT[F[_], G[_] : Functor, A](tfa: EitherT[F, E, A])(trans: F ~> G): EitherT[G, E, A] = EitherT(trans(tfa.value))
    }
  }

  implicit def readertTFunctor[E]: TFunctor[ReaderTCE[E]#l] = {
    new TFunctor[ReaderTCE[E]#l] {
      def mapTS[F[_] : Functor, G[_], A](tfa: ReaderT[F, E, A])(trans: F ~> G): ReaderT[G, E, A] = tfa.transform(trans)

      def mapTT[F[_], G[_] : Functor, A](tfa: ReaderT[F, E, A])(trans: F ~> G): ReaderT[G, E, A] = tfa.transform(trans)
    }
  }

  implicit def writertTFunctor[L: Monoid]: TFunctor[WriterTCL[L]#l] = {
    new TFunctor[WriterTCL[L]#l] {
      def mapTS[F[_] : Functor, G[_], A](tfa: WriterT[F, L, A])(trans: F ~> G): WriterT[G, L, A] = WriterT(trans(tfa.run))

      def mapTT[F[_], G[_] : Functor, A](tfa: WriterT[F, L, A])(trans: F ~> G): WriterT[G, L, A] = WriterT(trans(tfa.run))
    }
  }

  implicit def optiontTFunctor[E]: TFunctor[OptionT] = {
    new TFunctor[OptionT] {
      def mapTS[F[_] : Functor, G[_], A](tfa: OptionT[F, A])(trans: F ~> G): OptionT[G, A] = OptionT(trans(tfa.value))

      def mapTT[F[_], G[_] : Functor, A](tfa: OptionT[F, A])(trans: F ~> G): OptionT[G, A] = OptionT(trans(tfa.value))
    }
  }

  implicit def statetTFunctor[S]: TFunctor[StateTCS[S]#l] = {
    new TFunctor[StateTCS[S]#l] {
      def mapTS[F[_] : Functor, G[_], A](tfa: StateT[F, S, A])(trans: F ~> G): StateT[G, S, A] = {
        StateT.applyF[G, S, A](trans(tfa.runF.map(a => a.andThen(trans(_)))))
      }

      def mapTT[F[_], G[_] : Functor, A](tfa: StateT[F, S, A])(trans: F ~> G): StateT[G, S, A] = {
        StateT.applyF[G, S, A](trans(tfa.runF).map(a => a.andThen(trans(_))))
      }
    }
  }

}
