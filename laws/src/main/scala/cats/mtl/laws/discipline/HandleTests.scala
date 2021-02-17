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

import org.scalacheck.Prop.{forAll => ∀}
import org.scalacheck.{Arbitrary, Cogen}
import cats.kernel.laws.discipline.catsLawsIsEqToProp

trait HandleTests[F[_], E] extends RaiseTests[F, E] {
  implicit val handleInstance: Handle[F, E]

  override def laws: HandleLaws[F, E] = HandleLaws[F, E]

  def handle[A: Arbitrary](
      implicit ArbFA: Arbitrary[F[A]],
      ArbE: Arbitrary[E],
      CogenA: Cogen[A],
      CogenE: Cogen[E],
      EqFA: Eq[F[A]],
      EqEitherA: Eq[F[Either[E, A]]],
      EqEitherUnit: Eq[F[Either[E, Unit]]]): RuleSet = {
    new DefaultRuleSet(
      name = "handle",
      parent = Some(raise[A]),
      "raise and handleWith is function application" -> ∀(
        laws.raiseAndHandleWithIsFunctionApplication[A] _),
      "raise and handle is pure and function application" -> ∀(laws.raiseAndHandleIsPure[A] _),
      "pure and handleWith is pure" -> ∀(laws.handleWithPureIsPure[A] _),
      "pure and handle is pure" -> ∀(laws.handlePureIsPure[A] _),
      "pure and attempt is pure Right" -> ∀(laws.pureAttemptIsPureRight[A] _),
      "raise and attempt is pure Left" -> ∀(laws.raiseAttemptIsPureLeft _),
      "catch non fatal default" -> ∀(laws.catchNonFatalDefault[A] _)
    )
  }

}

object HandleTests {
  def apply[F[_], E](implicit instance0: Handle[F, E]): HandleTests[F, E] = {
    new HandleTests[F, E] {
      override val handleInstance: Handle[F, E] = instance0
      override val raiseInstance: Raise[F, E] = instance0
    }
  }
}
