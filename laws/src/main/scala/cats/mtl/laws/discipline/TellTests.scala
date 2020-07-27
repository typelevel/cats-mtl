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
import org.scalacheck.Arbitrary
import org.typelevel.discipline.Laws
import cats.kernel.laws.discipline.catsLawsIsEqToProp

trait TellTests[F[_], L] extends Laws {
  def laws: TellLaws[F, L]

  def tell[A: Arbitrary](
      implicit ArbL: Arbitrary[L],
      EqFA: Eq[F[A]]): RuleSet = {
    new DefaultRuleSet(
      name = "tell",
      parent = None,
      "tuple is (flip writer)" -> ∀(laws.tupleIsWriterFlipped[A] _),
      "writer is tell and map" -> ∀(laws.writerIsTellAndMap[A] _)
    )
  }

}

object TellTests {
  def apply[F[_], L](implicit instance0: Tell[F, L]): TellTests[F, L] = {
    new TellTests[F, L] {
      override def laws: TellLaws[F, L] = TellLaws[F, L]
    }
  }
}
