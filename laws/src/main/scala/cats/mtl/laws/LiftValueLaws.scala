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

import cats.laws.IsEq
import cats.laws.IsEqArrow

trait LiftValueLaws[F[_], G[_]] {
  implicit def lift: LiftValue[F, G]
  implicit final def F: Applicative[F] = lift.applicativeF
  implicit final def G: Applicative[G] = lift.applicativeG

  // external laws:
  def liftPureIsPure[A](value: A): IsEq[G[A]] =
    lift(F.pure(value)) <-> G.pure(value)

  def liftApIsApLift[A, B](fa: F[A], ff: F[A => B]): IsEq[G[B]] =
    lift(F.ap(ff)(fa)) <-> G.ap(lift(ff))(lift(fa))
}

object LiftValueLaws {
  def apply[F[_], G[_]](implicit liftValue: LiftValue[F, G]): LiftValueLaws[F, G] =
    new LiftValueLaws[F, G] {
      implicit val lift: LiftValue[F, G] = liftValue
    }
}
