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
package tests

import cats.arrow.FunctionK
import cats.data.Ior
import cats.mtl.laws.discipline._
import org.scalacheck._

class IorTTests extends BaseSuite {
  implicit val arbFunctionK: Arbitrary[Option ~> Option] =
    Arbitrary(
      Gen.oneOf(
        new (Option ~> Option) {
          def apply[A](fa: Option[A]): Option[A] = None
        },
        FunctionK.id[Option]))

  implicit def arbFunctionKIor[A]: Arbitrary[Ior[A, *] ~> Ior[A, *]] =
    Arbitrary(
      Gen.oneOf(
        new (Ior[A, *] ~> Ior[A, *]) {
          override def apply[B](fa: Ior[A, B]): Ior[A, B] = fa
        },
        FunctionK.id[Ior[A, *]]))

  {

    checkAll(
      "IorT[Option, String, *]",
      ChronicleTests[IorTC[Option, String]#l, String].chronicle[String]
    )

    checkAll(
      "Ior[String, String]",
      ChronicleTests[IorC[String]#l, String].chronicle[String]
    )

    checkAll(
      "WriterT[Ior[String, *], Int]",
      ChronicleTests[WriterTC[IorC[String]#l, Int]#l, String].chronicle[String]
    )

    checkAll(
      "WriterT[IorT[Option, String, *], Int]",
      ChronicleTests[WriterTC[IorTC[Option, String]#l, Int]#l, String].chronicle[String]
    )

  }
}
