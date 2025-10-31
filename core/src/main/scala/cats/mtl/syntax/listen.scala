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
package syntax

trait ListenSyntax {
  implicit def catsMtlSyntaxToListenOps[F[_], A](fa: F[A]): ListenOps[F, A] = new ListenOps(fa)

  @deprecated("use catsMtlSyntaxToListenOps", "1.7.0")
  def toListenOps[F[_], A](fa: F[A]): ListenOps[F, A] = catsMtlSyntaxToListenOps(fa)
}

final class ListenOps[F[_], A](val fa: F[A]) extends AnyVal {
  def listen[L](implicit listen: Listen[F, L]): F[(A, L)] =
    listen.listen(fa)

  def listens[L, B](f: L => B)(implicit listen: Listen[F, L]): F[(A, B)] =
    listen.listens(fa)(f)

}

object listen extends ListenSyntax
