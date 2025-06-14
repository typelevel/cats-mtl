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

import cats.arrow.FunctionK
import cats.laws.IsEq
import cats.laws.IsEqArrow

trait LiftKindLaws[From[_], To[_]] extends LiftValueLaws[From, To] {
  implicit def liftInstance: LiftKind[From, To]

  // external law:
  def limitedMapKIdentityIsPure[A](value: To[A]): IsEq[To[A]] =
    liftInstance.limitedMapK(value)(FunctionK.id) <-> value

  // internal laws:
  def limitedMapKLiftScopeConsistency[A](value: To[A], scope: From ~> From): IsEq[To[A]] =
    liftInstance.limitedMapK(value)(scope) <-> liftInstance.liftScope(scope)(value)

  def liftFLimitedMapKConsistency[A](value: From[A], scope: From ~> From): IsEq[To[A]] =
    liftInstance.liftF(scope(value)) <->
      liftInstance.limitedMapK(liftInstance.liftF(value))(scope)

  def limitedMapKIsReversible[A](
      value: From[A],
      scope: From ~> From): IsEq[Unlift.Result[From, A]] =
    unliftInstance.unlift(liftInstance.limitedMapK(liftInstance.liftF(value))(scope)) <->
      Unlift.success(scope(value))
}

object LiftKindLaws {
  def apply[From[_], To[_]](
      implicit lift: LiftKind[From, To],
      unlift: Unlift[To, From]
  ): LiftKindLaws[From, To] =
    new LiftKindLaws[From, To] {
      implicit val liftInstance: LiftKind[From, To] = lift
      implicit val unliftInstance: Unlift[To, From] = unlift
    }
}
