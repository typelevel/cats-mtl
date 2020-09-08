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

trait LocalTests[F[_], E] extends AskTests[F, E] {
  implicit val localInstance: Local[F, E]

  override def laws: LocalLaws[F, E] = LocalLaws[F, E]

  def local[A: Arbitrary, B](
      implicit ArbFA: Arbitrary[F[A]],
      ArbFAB: Arbitrary[F[A => B]],
      ArbEE: Arbitrary[E => E],
      ArbE: Arbitrary[E],
      CogenE: Cogen[E],
      EqFU: Eq[F[E]],
      EqFA: Eq[F[A]],
      EqFB: Eq[F[B]]): RuleSet = {
    new DefaultRuleSet(
      name = "local",
      parent = Some(ask[A]),
      "ask reflects local" -> ∀(laws.askReflectsLocal _),
      "local pure is pure" -> ∀(laws.localPureIsPure[A] _),
      "local distributes over ap" -> ∀(laws.localDistributesOverAp[A, B] _),
      "scope is local const" -> ∀(laws.scopeIsLocalConst[A] _)
    )
  }

}

object LocalTests {
  def apply[F[_], E](implicit instance0: Local[F, E]): LocalTests[F, E] = {
    new LocalTests[F, E] {
      override lazy val localInstance: Local[F, E] = instance0
      override lazy val askInstance: Ask[F, E] = instance0
    }
  }
}
