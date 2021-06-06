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

package cats.mtl.tests

import cats.effect.testkit.TestInstances
import cats.mtl.effect.instances.io._
import cats.effect.IO
import cats.effect.IOLocal
import munit.CatsEffectSuite
import munit.DisciplineSuite

class IOTests extends CatsEffectSuite with DisciplineSuite with TestInstances {
  import cats.mtl.laws.discipline.{HandleTests, StatefulTests}

  implicit val ticker: Ticker = Ticker()

  // Terrible hack!
  implicit val local: IOLocal[Int] = IOLocal(0).unsafeRunSync()(ioRuntime)
  checkAll("Stateful[IO]", StatefulTests[IO, Int].stateful)
  checkAll("Handle[IO]", HandleTests[IO, Throwable].handle[Int])
  
}
