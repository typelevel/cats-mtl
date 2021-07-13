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

package cats.mtl.effect.instances

import cats.mtl.Stateful
import cats.effect.IO
import cats.Monad
import cats.effect.IOLocal
import cats.mtl.Raise
import cats.Functor

object io extends IOInstances

trait IOInstances {
  implicit def catsMtlEffectStatefulForIO[A](implicit local: IOLocal[A]): Stateful[IO, A] =
    new Stateful[IO, A] {
      override def monad: Monad[IO] = IO.asyncForIO
      override def get: IO[A] = local.get
      override def set(s: A): IO[Unit] = local.set(s)
    }

  implicit def catsMtlEffectRaiseForIO: Raise[IO, Throwable] =
    new Raise[IO, Throwable] {
      override def functor: Functor[IO] = IO.asyncForIO
      override def raise[E2 <: Throwable, A](e: E2): IO[A] =
        IO.raiseError(e)
    }
}
