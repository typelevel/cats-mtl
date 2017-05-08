package cats

import cats.mtl.evidence._
import cats.data._

package object mtl {

  type CurryT[T[_[_], _], M[_]] = {type l[A] = T[M, A]}

  type Ask[E, M[_]] = AskN[Find[EffAsk[E], M]#Out, M, E]
  type Handle[E, M[_]] = HandleN[Find[EffHandle[E], M]#Out, M, E]
  type Listen[E, M[_]] = ListenN[Find[EffListen[E], M]#Out, M, E]
  type Local[E, M[_]] = LocalN[Find[EffLocal[E], M]#Out, M, E]
  type Raise[E, M[_]] = RaiseN[Find[EffRaise[E], M]#Out, M, E]

  type ReaderTC[E] = {type l[M[_], A] = ReaderT[M, E, A]}
  type StateTC[S] = {type l[M[_], A] = StateT[M, S, A]}
  type EitherTC[E] = {type l[M[_], A] = EitherT[M, E, A]}
  type WriterTC[L] = {type l[M[_], A] = WriterT[M, L, A]}

}
