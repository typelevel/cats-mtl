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

import cats.data.EitherT

trait HandleSyntax {
  implicit def catsMtlSyntaxToHandleOps[F[_], A](fa: F[A]): HandleOps[F, A] = new HandleOps(fa)

  @deprecated("use catsMtlSyntaxToHandleOps", "1.6.1") def toHandleOps[F[_], A](
      fa: F[A]): HandleOps[F, A] = catsMtlSyntaxToHandleOps(fa)
}

final class HandleOps[F[_], A](val fa: F[A]) extends AnyVal {
  def attemptHandle[E](implicit handle: Handle[F, E]): F[Either[E, A]] =
    handle.attempt(fa)
  def attemptHandleT[E](implicit handle: Handle[F, E]): EitherT[F, E, A] =
    handle.attemptT(fa)
  def handle[E](f: E => A)(implicit handle: Handle[F, E]): F[A] =
    handle.handle(fa)(f)
  def handleWith[E](f: E => F[A])(implicit handle: Handle[F, E]): F[A] =
    handle.handleWith(fa)(f)
}

object handle extends HandleSyntax
