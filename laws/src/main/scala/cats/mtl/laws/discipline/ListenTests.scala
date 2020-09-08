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
import cats.kernel.laws.discipline.catsLawsIsEqToProp

trait ListenTests[F[_], L] extends TellTests[F, L] {
  def laws: ListenLaws[F, L]

  def listen[A: Arbitrary, B: Arbitrary](
      implicit ArbFA: Arbitrary[F[A]],
      ArbL: Arbitrary[L],
      CogenL: Cogen[L],
      EqFA: Eq[F[A]],
      EqFAB: Eq[F[(A, B)]],
      EqFUL: Eq[F[(Unit, L)]]): RuleSet = {
    new DefaultRuleSet(
      name = "listen",
      parent = Some(tell[A]),
      "listen respects tell" -> ∀(laws.listenRespectsTell _),
      "listen adds no effects" -> ∀(laws.listenAddsNoEffects[A] _),
      "listens is listen then map" -> ∀(laws.listensIsListenThenMap[A, B] _)
    )
  }

}

object ListenTests {
  def apply[F[_], L](implicit instance0: Listen[F, L]): ListenTests[F, L] = {
    new ListenTests[F, L] {
      override def laws: ListenLaws[F, L] = ListenLaws[F, L]
    }
  }
}
