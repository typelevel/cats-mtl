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

trait ApplicativeLocalTests[F[_], E] extends ApplicativeAskTests[F, E] {
  implicit val localInstance: ApplicativeLocal[F, E]

  override def laws: ApplicativeLocalLaws[F, E] = ApplicativeLocalLaws[F, E]

  def applicativeLocal[A: Arbitrary, B](
      implicit ArbFA: Arbitrary[F[A]],
      ArbFAB: Arbitrary[F[A => B]],
      ArbEE: Arbitrary[E => E],
      ArbE: Arbitrary[E],
      CogenA: Cogen[A],
      CogenE: Cogen[E],
      EqFU: Eq[F[E]],
      EqFA: Eq[F[A]],
      EqFB: Eq[F[B]]): RuleSet = {
    new DefaultRuleSet(
      name = "applicativeLocal",
      parent = Some(applicativeAsk[A]),
      "ask reflects local" -> ∀(laws.askReflectsLocal _),
      "local pure is pure" -> ∀(laws.localPureIsPure[A] _),
      "local distributes over ap" -> ∀(laws.localDistributesOverAp[A, B] _),
      "scope is local const" -> ∀(laws.scopeIsLocalConst[A] _)
    )
  }

}

object ApplicativeLocalTests {
  def apply[F[_], E](
      implicit instance0: ApplicativeLocal[F, E]): ApplicativeLocalTests[F, E] = {
    new ApplicativeLocalTests[F, E] {
      override lazy val localInstance: ApplicativeLocal[F, E] = instance0
      override lazy val askInstance: ApplicativeAsk[F, E] = instance0
    }
  }
}
