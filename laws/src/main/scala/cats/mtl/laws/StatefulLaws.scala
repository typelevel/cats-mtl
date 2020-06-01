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

import cats.laws.IsEq
import cats.laws.IsEqArrow
import cats.syntax.functor._
import cats.syntax.flatMap._
import cats.syntax.apply._

trait StatefulLaws[F[_], S] {
  implicit val stateInstance: Stateful[F, S]
  implicit val monad: Monad[F] = stateInstance.monad

  import stateInstance._
  import monad.pure

  // external laws:
  def getThenSetDoesNothing: IsEq[F[Unit]] =
    (get >>= set) <-> pure(())

  def setThenGetReturnsSetted(s: S): IsEq[F[S]] =
    (set(s) *> get) <-> (set(s) *> pure(s))

  def setThenSetSetsLast(s1: S, s2: S): IsEq[F[Unit]] =
    set(s1) *> set(s2) <-> set(s2)

  def getThenGetGetsOnce: IsEq[F[S]] =
    get *> get <-> get

  // internal law:
  def modifyIsGetThenSet(f: S => S): IsEq[F[Unit]] =
    modify(f) <-> ((get map f) flatMap set)
}

object StatefulLaws {
  def apply[F[_], S](implicit instance0: Stateful[F, S]): StatefulLaws[F, S] =
    new StatefulLaws[F, S] {
      override lazy val stateInstance: Stateful[F, S] = instance0
    }
}
