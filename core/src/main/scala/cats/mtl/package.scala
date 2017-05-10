package cats

import cats.data._

package object mtl {

  type CurryT[T[_[_], _], M[_]] = {type l[A] = T[M, A]}

  type ReaderTC[E] = {type l[M[_], A] = ReaderT[M, E, A]}
  type StateTC[S] = {type l[M[_], A] = StateT[M, S, A]}
  type EitherTC[E] = {type l[M[_], A] = EitherT[M, E, A]}
  type WriterTC[L] = {type l[M[_], A] = WriterT[M, L, A]}

}
