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
package syntax

trait LocalSyntax {
  implicit def toLocalOps[F[_], A](fa: F[A]): LocalOps[F, A] = new LocalOps(fa)
}

final class LocalOps[F[_], A](val fa: F[A]) extends AnyVal {
  def local[E](f: E => E)(implicit applicativeLocal: ApplicativeLocal[F, E]): F[A] =
    applicativeLocal.local(fa)(f)
  def scope[E](e: E)(implicit applicativeLocal: ApplicativeLocal[F, E]): F[A] =
    applicativeLocal.scope(fa)(e)
}

object local extends LocalSyntax
