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
import org.typelevel.discipline.Laws

trait LiftValueTests[From[_], To[_]] extends Laws {
  implicit val liftInstance: LiftValue[From, To]
  implicit val unliftInstance: Unlift[To, From]

  def laws: LiftValueLaws[From, To] = LiftValueLaws[From, To]

  def liftValue[A](
      implicit arbFromA: Arbitrary[From[A]],
      eqResultFromA: Eq[Unlift.Result[From, A]],
      eqToA: Eq[To[A]]
  ): RuleSet =
    new SimpleRuleSet(
      name = "liftValue",
      "liftF and liftK are consistent" -> ∀(laws.liftFLiftKConsistency[A] _),
      "liftF is reversible" -> ∀(laws.liftFIsReversible[A] _)
    )
}

object LiftValueTests {
  def apply[From[_], To[_]](
      implicit lift: LiftValue[From, To],
      unlift: Unlift[To, From]
  ): LiftValueTests[From, To] =
    new LiftValueTests[From, To] {
      implicit val liftInstance: LiftValue[From, To] = lift
      implicit val unliftInstance: Unlift[To, From] = unlift
    }
}
