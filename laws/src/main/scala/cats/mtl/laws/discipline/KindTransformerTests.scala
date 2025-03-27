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
import org.scalacheck.Prop.{forAll => ∀}
import org.scalacheck.Arbitrary
import org.typelevel.discipline.Laws

trait KindTransformerTests[F[_], G[_]] extends Laws {
  implicit val ktInstance: KindTransformer[F, G]

  def laws: KindTransformerLaws[F, G] = KindTransformerLaws[F, G]

  def kindTransformer[A: Arbitrary](
      implicit arbFA: Arbitrary[F[A]],
      arbGA: Arbitrary[G[A]],
      arbFF: Arbitrary[F ~> F],
      eqGA: Eq[G[A]]
  ): RuleSet =
    new DefaultRuleSet(
      name = "kindTransformer",
      parent = None,
      "limitedMapK identity is pure" -> ∀(laws.limitedMapKIdentityIsPure[A] _),
      "liftK then limitedMapK is map then liftK" -> ∀(
        laws.liftKThenLimitedMapKIsMapThenLiftK[A] _),
      "liftFunctionK apply is limitedMapK" -> ∀(laws.liftFunctionKApplyIsLimitedMapK[A] _)
    )
}

object KindTransformerTests {
  def apply[F[_], G[_]](
      implicit instance: KindTransformer[F, G]): KindTransformerTests[F, G] = {
    new KindTransformerTests[F, G] {
      val ktInstance = instance
    }
  }
}
