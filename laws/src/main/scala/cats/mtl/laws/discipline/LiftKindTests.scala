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

trait LiftKindTests[F[_], G[_]] extends LiftValueTests[F, G] {
  implicit val liftInstance: LiftKind[F, G]

  override def laws: LiftKindLaws[F, G] = LiftKindLaws[F, G]

  def liftKind[A, B](
      implicit arbA: Arbitrary[A],
      arbFA: Arbitrary[F[A]],
      arbFAB: Arbitrary[F[A => B]],
      arbGA: Arbitrary[G[A]],
      arbFF: Arbitrary[F ~> F],
      eqGA: Eq[G[A]],
      eqGB: Eq[G[B]]
  ): RuleSet =
    new DefaultRuleSet(
      name = "liftKind",
      parent = Some(liftValue[A, B]),
      "limitedMapK(identity) is pure" -> ∀(laws.limitedMapKIdentityIsPure[A] _),
      "lift(scope) is limitedMapK(lift)(scope)" -> ∀(laws.liftScopeIsLimitedMapKLiftScope[A] _),
      "limitedMapK and liftScope are consistent" -> ∀(laws.limitedMapKLiftScopeConsistency[A] _)
    )
}

object LiftKindTests {
  def apply[F[_], G[_]](implicit lift: LiftKind[F, G]): LiftKindTests[F, G] =
    new LiftKindTests[F, G] {
      implicit val liftInstance: LiftKind[F, G] = lift
    }

  implicit val arbitraryFunctionKListList: Arbitrary[List ~> List] =
    Arbitrary(ListGens.genFunctionKListList)
}
