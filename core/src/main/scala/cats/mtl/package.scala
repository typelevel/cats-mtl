package cats

import cats.data._

package object mtl {

  type CurryT[T[_[_], _], M[_]] = {type l[A] = T[M, A]}

//  type Ask[E, M[_]] = AskN[_ <: Nat, M, E]
//  type Handle[E, M[_]] = HandleN[_, M, E]
//  type Listen[E, M[_]] = ListenN[_, M, E]
//  type Local[E, M[_]] = LocalN[_, M, E]
//  type Raise[E, M[_]] = RaiseN[_, M, E]

  type ReaderTC[E] = {type l[M[_], A] = ReaderT[E, M, A]}
  type StateTC[S] = {type l[M[_], A] = StateT[M, S, A]}
  type EitherTC[E] = {type l[M[_], A] = EitherT[M, E, A]}
  type WriterTC[L] = {type l[M[_], A] = WriterT[M, L, A]}

}
