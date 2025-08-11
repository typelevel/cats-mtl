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
import org.typelevel.discipline.Laws

trait LiftValueTests[F[_], G[_]] extends Laws {
  implicit val liftInstance: LiftValue[F, G]
  implicit final def F: Applicative[F] = liftInstance.applicativeF
  implicit final def G: Applicative[G] = liftInstance.applicativeG

  def laws: LiftValueLaws[F, G] = LiftValueLaws[F, G]

  def liftValue[A, B](
      implicit arbA: Arbitrary[A],
      arbFA: Arbitrary[F[A]],
      arbFAB: Arbitrary[F[A => B]],
      eqGA: Eq[G[A]],
      eqGB: Eq[G[B]]
  ): RuleSet =
    new SimpleRuleSet(
      name = "liftValue",
      "lift(pure) is pure" -> ∀(laws.liftPureIsPure[A] _),
      "lift(ap) is ap(lift, lift)" -> ∀(laws.liftApIsApLift[A, B] _)
    )
}

object LiftValueTests {
  def apply[F[_], G[_]](implicit lift: LiftValue[F, G]): LiftValueTests[F, G] =
    new LiftValueTests[F, G] {
      implicit val liftInstance: LiftValue[F, G] = lift
    }
}
