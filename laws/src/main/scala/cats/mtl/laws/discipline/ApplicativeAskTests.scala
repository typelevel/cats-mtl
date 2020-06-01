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
package mtl
package laws
package discipline

import org.scalacheck.Prop.{forAll => ∀}
import org.scalacheck.{Arbitrary, Cogen}
import org.typelevel.discipline.Laws
import cats.kernel.laws.discipline.catsLawsIsEqToProp

trait ApplicativeAskTests[F[_], E] extends Laws {
  implicit val askInstance: ApplicativeAsk[F, E]

  def laws: ApplicativeAskLaws[F, E] = ApplicativeAskLaws[F, E]

  def applicativeAsk[A: Arbitrary](
      implicit ArbFA: Arbitrary[F[A]],
      ArbE: Arbitrary[E],
      CogenA: Cogen[A],
      CogenE: Cogen[E],
      EqFU: Eq[F[E]],
      EqFA: Eq[F[A]]): RuleSet = {
    new DefaultRuleSet(
      name = "applicativeAsk",
      parent = None,
      "ask adds no effects" -> ∀(laws.askAddsNoEffects[A] _),
      "reader is ask and map" -> ∀(laws.readerIsAskAndMap[A] _)
    )
  }

}

object ApplicativeAskTests {
  def apply[F[_], E](implicit instance: ApplicativeAsk[F, E]): ApplicativeAskTests[F, E] = {
    new ApplicativeAskTests[F, E] {
      val askInstance = instance
    }
  }
}
