package cats

import cats.mtl.evidence._
import cats.data._

package object mtl {

  type Find[Eff, M[_]] = FindTrue[MapCanDo[Eff, M]#Out]
  type CurryT[T[_[_], _], M[_]] = {type l[A] = T[M, A]}
  type Ask[E, M[_]] = AskN[Find[EffAsk[E], M]#Out, M, E]
  type Local[E, M[_]] = LocalN[Find[EffLocal[E], M]#Out, M, E]
  type ReaderTC[E] = {type l[M[_], A] = ReaderT[M, E, A]}
  type StateTC[S] = {type l[M[_], A] = StateT[M, S, A]}
  type EitherTC[E] = {type l[M[_], A] = EitherT[M, E, A]}
  type WriterTC[L] = {type l[M[_], A] = WriterT[M, L, A]}

}
