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

trait RaiseLaws[F[_], E] {
  implicit val raiseInstance: Raise[F, E]
  implicit val functor: Functor[F] = raiseInstance.functor

  import raiseInstance._

  // free law:
  def failThenFlatMapFails[A](ex: E, f: A => A): IsEq[F[A]] =
    raise(ex).map(f) <-> raise(ex)

}

object RaiseLaws {
  def apply[F[_], E](implicit instance0: Raise[F, E]): RaiseLaws[F, E] = {
    new RaiseLaws[F, E] {
      override lazy val raiseInstance: Raise[F, E] = instance0
    }
  }
}
