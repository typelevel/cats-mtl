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
import cats.syntax.apply._
import cats.syntax.functor._

trait AskLaws[F[_], E] {
  implicit val askInstance: Ask[F, E]
  import askInstance._
  implicit val applicative: Applicative[F] = askInstance.applicative

  // external law:
  def askAddsNoEffects[A](fa: F[A]): IsEq[F[A]] =
    ask *> fa <-> fa

  // internal law:
  def readerIsAskAndMap[A](f: E => A): IsEq[F[A]] =
    ask.map(f) <-> reader(f)
}

object AskLaws {
  def apply[F[_], E](implicit instance0: Ask[F, E]): AskLaws[F, E] = {
    new AskLaws[F, E] {
      override lazy val askInstance = instance0
    }
  }
}
