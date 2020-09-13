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

import org.scalacheck.{Arbitrary, Cogen}
import org.scalacheck.Prop.{forAll => ∀}
import org.typelevel.discipline.Laws
import cats.kernel.laws.discipline.catsLawsIsEqToProp

trait StatefulTests[F[_], S] extends Laws {
  implicit val stateInstance: Stateful[F, S]

  def laws: StatefulLaws[F, S] = StatefulLaws[F, S]

  def stateful[A: Arbitrary](
      implicit ArbS: Arbitrary[S],
      CogenS: Cogen[S],
      EqFU: Eq[F[Unit]],
      EqFS: Eq[F[S]]): RuleSet = {
    new DefaultRuleSet(
      name = "stateful",
      parent = None,
      "get then set has does nothing" -> laws.getThenSetDoesNothing,
      "set then get returns the setted value" -> ∀(laws.setThenGetReturnsSetted _),
      "set then set sets the last value" -> ∀(laws.setThenSetSetsLast _),
      "get then get gets once" -> laws.getThenGetGetsOnce,
      "modify is get then set" -> ∀(laws.modifyIsGetThenSet _)
    )
  }

}

object StatefulTests {
  def apply[F[_], S](implicit instance0: Stateful[F, S]): StatefulTests[F, S] = {
    new StatefulTests[F, S] with Laws {
      override implicit val stateInstance: Stateful[F, S] = instance0
    }
  }
}
