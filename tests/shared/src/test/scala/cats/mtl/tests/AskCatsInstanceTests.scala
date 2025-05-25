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

import cats.laws.discipline.{ApplicativeTests, MonadTests}
import org.scalacheck.Arbitrary._
import org.scalacheck._

class AskCatsInstanceTests extends BaseSuite {

  private implicit def arbAsk[F[_]: Applicative, A: Arbitrary]: Arbitrary[Ask[F, A]] =
    Arbitrary {
      arbitrary[A].map(Ask.const[F, A](_))
    }

  private implicit def eqAsk[F[_], A](implicit E: Eq[F[A]]): Eq[Ask[F, A]] = Eq.by(_.ask)

  checkAll(
    "Applicative[Ask[Option, *]]",
    ApplicativeTests[Ask[Option, *]](
      using Ask.applicativeAsk[Option] // make sure we test the weaker applicative instance
    ).applicative[String, String, String]
  )

  checkAll("Monad[Ask[Option, *]]", MonadTests[Ask[Option, *]].monad[String, String, String])

}
