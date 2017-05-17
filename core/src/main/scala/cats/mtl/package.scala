package cats

import cats.data._

package object mtl {

  type CurryT[T[_[_], _], M[_]] = {type l[A] = T[M, A]}

  type ReaderTCE[E] = {type l[M[_], A] = ReaderT[M, E, A]}
  type ReaderTC[M[_], E] = {type l[A] = ReaderT[M, E, A]}
  type ReaderC[E] = {type l[A] = Reader[E, A]}
  type OptionTC[M[_]] = {type l[A] = OptionT[M, A]}
  type StateTC[M[_], S] = {type l[A] = StateT[M, S, A]}
  type StateTCS[S] = {type l[M[_], A] = StateT[M, S, A]}
  type EitherC[E] = {type l[A] = E Either A}
  type EitherTC[M[_], E] = {type l[A] = EitherT[M, E, A]}
  type EitherTCE[E] = {type l[M[_], A] = EitherT[M, E, A]}
  type WriterTC[M[_], L] = {type l[A] = WriterT[M, L, A]}
  type WriterTCL[L] = {type l[M[_], A] = WriterT[M, L, A]}
  type WriterC[L] = {type l[A] = Writer[L, A]}
  type of[F[_], G[_]] = {type l[A] = F[G[A]]}

}
