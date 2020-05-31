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

trait ApplicativeCensorTests[F[_], L] extends FunctorListenTests[F, L] {
  def laws: ApplicativeCensorLaws[F, L]

  def applicativeCensor[A: Arbitrary, B: Arbitrary](
      implicit ArbFA: Arbitrary[F[A]],
      ArbL: Arbitrary[L],
      CogenA: Cogen[A],
      CogenL: Cogen[L],
      EqFU: Eq[F[Unit]],
      EqFA: Eq[F[A]],
      EqFAB: Eq[F[(A, B)]],
      EqFUL: Eq[F[(Unit, L)]]): RuleSet = {
    new DefaultRuleSet(
      name = "applicativeCensor",
      parent = Some(functorListen[A, B]),
      "tell leftProduct is tell combined" -> ∀(laws.tellLeftProductHomomorphism _),
      "tell rightProduct is tell combined" -> ∀(laws.tellRightProductHomomorphism _),
      "censor with pure is tell with empty" -> ∀(laws.censorWithPurIsTellEmpty[A] _),
      "tell and clear is pure unit" -> ∀(laws.tellAndClearIsPureUnit _),
      "clear is idempotent" -> ∀(laws.clearIsIdempotent[A] _)
    )
  }

}

object ApplicativeCensorTests {
  def apply[F[_], L](
      implicit instance0: ApplicativeCensor[F, L]): ApplicativeCensorTests[F, L] = {
    new ApplicativeCensorTests[F, L] {
      override def laws: ApplicativeCensorLaws[F, L] = ApplicativeCensorLaws[F, L]
    }
  }
}
