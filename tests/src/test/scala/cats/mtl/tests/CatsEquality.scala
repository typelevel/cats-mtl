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
package tests

import org.scalactic._
import TripleEqualsSupport.AToBEquivalenceConstraint
import TripleEqualsSupport.BToAEquivalenceConstraint

// pasted from cats

// The code in this file was taken and only slightly modified from
// https://github.com/bvenners/equality-integration-demo
// Thanks for the great examples, Bill!

final class CatsEquivalence[T](T: Eq[T]) extends Equivalence[T] {
  def areEquivalent(a: T, b: T): Boolean = T.eqv(a, b)
}

trait LowPriorityStrictCatsConstraints extends TripleEquals {
  implicit final def lowPriorityCatsCanEqual[A, B](
      implicit B: Eq[B],
      ev: A <:< B): CanEqual[A, B] =
    new AToBEquivalenceConstraint[A, B](new CatsEquivalence(B), ev)
}

trait StrictCatsEquality extends LowPriorityStrictCatsConstraints {
  override def convertToEqualizer[T](left: T): Equalizer[T] = super.convertToEqualizer[T](left)
  implicit override def convertToCheckingEqualizer[T](left: T): CheckingEqualizer[T] =
    new CheckingEqualizer(left)
  override def unconstrainedEquality[A, B](implicit equalityOfA: Equality[A]): CanEqual[A, B] =
    super.unconstrainedEquality[A, B]
  implicit final def catsCanEqual[A, B](implicit A: Eq[A], ev: B <:< A): CanEqual[A, B] =
    new BToAEquivalenceConstraint[A, B](new CatsEquivalence(A), ev)
}

object StrictCatsEquality extends StrictCatsEquality
