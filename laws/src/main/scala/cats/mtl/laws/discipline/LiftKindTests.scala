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
package discipline

import cats.kernel.laws.discipline.catsLawsIsEqToProp
import org.scalacheck.Arbitrary
import org.scalacheck.Prop.{forAll => ∀}

trait LiftKindTests[From[_], To[_]] extends LiftValueTests[From, To] {
  implicit val liftInstance: LiftKind[From, To]

  override def laws: LiftKindLaws[From, To] = LiftKindLaws[From, To]

  def liftKind[A](
      implicit arbFromA: Arbitrary[From[A]],
      arbToA: Arbitrary[To[A]],
      arbFromFrom: Arbitrary[From ~> From],
      eqResultFromA: Eq[Unlift.Result[From, A]],
      eqToA: Eq[To[A]]
  ): RuleSet =
    new DefaultRuleSet(
      name = "liftKind",
      parent = Some(liftValue[A]),
      "limitedMapK with identity is pure" -> ∀(laws.limitedMapKIdentityIsPure[A] _),
      "limitedMapK and liftScope are consistent" ->
        ∀(laws.limitedMapKLiftScopeConsistency[A] _),
      "liftF and limitedMapK are consistent" -> ∀(laws.liftFLimitedMapKConsistency[A] _),
      "limitedMapK is reversible" -> ∀(laws.limitedMapKIsReversible[A] _)
    )
}

object LiftKindTests {
  def apply[From[_], To[_]](
      implicit lift: LiftKind[From, To],
      unlift: Unlift[To, From]
  ): LiftKindTests[From, To] =
    new LiftKindTests[From, To] {
      implicit val liftInstance: LiftKind[From, To] = lift
      implicit val unliftInstance: Unlift[To, From] = unlift
    }

  implicit val arbitraryFunctionKListList: Arbitrary[List ~> List] =
    Arbitrary(ListGens.genFunctionKListList)
}
