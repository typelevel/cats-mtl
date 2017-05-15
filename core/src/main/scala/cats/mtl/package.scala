package cats

import cats.data._

package object mtl {

  type CurryT[T[_[_], _], M[_]] = {type l[A] = T[M, A]}

  type ReaderTCE[E] = {type l[M[_], A] = ReaderT[M, E, A]}
  type ReaderTC[M[_], E] = {type l[A] = ReaderT[M, E, A]}
  type ReaderC[E] = {type l[A] = Reader[E, A]}
  type StateTC[S] = {type l[M[_], A] = StateT[M, S, A]}
  type EitherTC[E] = {type l[M[_], A] = EitherT[M, E, A]}
  type WriterTC[L] = {type l[M[_], A] = WriterT[M, L, A]}
  type of[F[_], G[_]] = {type l[A] = F[G[A]]}

}
