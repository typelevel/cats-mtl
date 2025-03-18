/*
 * Copyright 2021 Typelevel
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
package mtl
package laws

import cats.laws.IsEq
import cats.laws.IsEqArrow

trait LiftValueLaws[From[_], To[_]] {
  implicit def liftInstance: LiftValue[From, To]
  implicit def unliftInstance: Unlift[To, From]
  implicit final def functor: Functor[From] = unliftInstance.functor

  // internal laws:
  def liftFLiftKConsistency[A](value: From[A]): IsEq[To[A]] =
    liftInstance.liftF(value) <-> liftInstance.liftK(value)

  def liftFIsReversible[A](value: From[A]): IsEq[Unlift.Result[From, A]] =
    unliftInstance.unlift(liftInstance.liftF(value)) <-> Unlift.success(value)
}

object LiftValueLaws {
  def apply[From[_], To[_]](
      implicit lift: LiftValue[From, To],
      unlift: Unlift[To, From]
  ): LiftValueLaws[From, To] =
    new LiftValueLaws[From, To] {
      implicit val liftInstance: LiftValue[From, To] = lift
      implicit val unliftInstance: Unlift[To, From] = unlift
    }
}
