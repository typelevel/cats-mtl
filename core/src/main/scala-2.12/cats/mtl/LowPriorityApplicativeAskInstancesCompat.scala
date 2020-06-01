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

package cats.mtl

import cats.{Applicative, Id}
import cats.data.{Kleisli, Reader}

private[mtl] trait LowPriorityAskInstancesCompat {
  implicit def askForReader[E]: Ask[Reader[E, *], E] =
    new Ask[Reader[E, *], E] {
      val applicative = Applicative[Reader[E, *]]
      def ask = Kleisli.ask[Id, E]
    }
}
