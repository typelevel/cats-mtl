/*
 * Copyright 2020 Typelevel
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cats

import cats.data._

import scala.collection.immutable.SortedMap

package object mtl {

  private[mtl] type CurryT[T[_[_], _], M[_]] = { type l[A] = T[M, A] }

  // Curried type operators
  private[mtl] type ReaderTCE[E] = { type l[M[_], A] = ReaderT[M, E, A] }
  private[mtl] type ReaderTC[M[_], E] = { type l[A] = ReaderT[M, E, A] }
  private[mtl] type ReaderC[E] = { type l[A] = Reader[E, A] }
  private[mtl] type OptionTC[M[_]] = { type l[A] = OptionT[M, A] }
  private[mtl] type StateC[S] = { type l[A] = State[S, A] }
  private[mtl] type StateTC[M[_], S] = { type l[A] = StateT[M, S, A] }
  private[mtl] type StateTCS[S] = { type l[M[_], A] = StateT[M, S, A] }
  private[mtl] type EitherC[E] = { type l[A] = E Either A }
  private[mtl] type EitherTC[M[_], E] = { type l[A] = EitherT[M, E, A] }
  private[mtl] type EitherTCE[E] = { type l[M[_], A] = EitherT[M, E, A] }
  private[mtl] type TupleC[L] = { type l[A] = (L, A) }
  private[mtl] type FunctionC[E] = { type l[A] = E => A }
  private[mtl] type WriterC[L] = { type l[A] = Writer[L, A] }
  private[mtl] type WriterTC[M[_], L] = { type l[A] = WriterT[M, L, A] }
  private[mtl] type WriterTCL[L] = { type l[M[_], A] = WriterT[M, L, A] }
  private[mtl] type of[F[_], G[_]] = { type l[A] = F[G[A]] }
  private[mtl] type NestedC[F[_], G[_]] = { type l[A] = Nested[F, G, A] }
  private[mtl] type MapC[K] = { type l[V] = Map[K, V] }
  private[mtl] type SortedMapC[K] = { type l[V] = SortedMap[K, V] }
  private[mtl] type IorTC[M[_], A] = { type l[B] = IorT[M, A, B] }
  private[mtl] type IorC[A] = { type l[B] = Ior[A, B] }
}
