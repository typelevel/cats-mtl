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

trait RaiseTests[F[_], E] extends Laws {
  implicit val raiseInstance: Raise[F, E]
  def laws: RaiseLaws[F, E] = RaiseLaws[F, E]

  def raise[A: Arbitrary](
      implicit ArbFA: Arbitrary[F[A]],
      ArbE: Arbitrary[E],
      CogenA: Cogen[A],
      EqFU: Eq[F[Unit]],
      EqFA: Eq[F[A]]): RuleSet = {
    new DefaultRuleSet(
      name = "raise",
      parent = None,
      "catch non fatal default" -> ∀(laws.failThenFlatMapFails[A] _)
    )
  }

}

object RaiseTests {
  def apply[F[_], E](implicit instance0: Raise[F, E]): RaiseTests[F, E] = {
    new RaiseTests[F, E] with Laws {
      override implicit val raiseInstance: Raise[F, E] = instance0
    }
  }
}
