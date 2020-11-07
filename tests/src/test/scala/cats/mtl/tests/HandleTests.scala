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

import cats.data.{Kleisli, WriterT}

class HandleTests extends BaseSuite {
  test("handleForApplicativeError") {
    case class Foo[A](bar: A)

    implicit def fooApplicativeError: ApplicativeError[Foo, String] =
      new ApplicativeError[Foo, String] {
        def ap[A, B](ff: Foo[A => B])(fa: Foo[A]): Foo[B] = ???

        def pure[A](x: A): Foo[A] = ???

        def raiseError[A](e: String): Foo[A] = ???

        def handleErrorWith[A](fa: Foo[A])(f: String => Foo[A]): Foo[A] = ???
      }

    Handle[Foo, String]
    Handle[Kleisli[Foo, Unit, *], String]
    Handle[WriterT[Foo, String, *], String]
  }
}
