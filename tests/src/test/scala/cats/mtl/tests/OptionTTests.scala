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

import cats._
import cats.arrow.FunctionK
import cats.data._
import cats.laws.discipline.arbitrary._
import org.scalacheck._

class OptionTTests extends BaseSuite {
  import cats.mtl.laws.discipline.HandleTests

  implicit val arbFunctionK: Arbitrary[Option ~> Option] =
    Arbitrary(
      Gen.oneOf(
        new (Option ~> Option) {
          def apply[A](fa: Option[A]): Option[A] = None
        },
        FunctionK.id[Option]))

  {

    checkAll("Option", HandleTests[Option, Unit].handle[Int])
    checkAll(
      "OptionT[Either[String, *], *]",
      HandleTests[OptionT[Either[String, *], *], Unit].handle[Int])

    checkAll("WriterT[Option, Int, *]", HandleTests[WriterT[Option, Int, *], Unit].handle[Int])

    checkAll(
      "WriterT[OptionT[Either[String, *], *], Int, *]",
      HandleTests[WriterT[OptionT[Either[String, *], *], Int, *], Unit].handle[Int])

  }
}
