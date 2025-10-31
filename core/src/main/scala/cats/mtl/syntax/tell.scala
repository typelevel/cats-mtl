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

trait TellSyntax {
  implicit def catsMtlSyntaxToTellOps[L](e: L): TellOps[L] = new TellOps(e)
  implicit def catsMtlSyntaxToTupleOps[L, A](t: (L, A)): TupleOps[L, A] = new TupleOps(t)

  @deprecated("use catsMtlSyntaxToTellOps", "1.7.0")
  def toTellOps[L](e: L): TellOps[L] =
    catsMtlSyntaxToTellOps(e)
  @deprecated("use catsMtlSyntaxToTupleOps", "1.7.0")
  def toTupleOps[L, A](t: (L, A)): TupleOps[L, A] = catsMtlSyntaxToTupleOps(t)
}

final class TupleOps[L, A](val t: (L, A)) extends AnyVal {
  def tuple[F[_]](implicit tell: Tell[F, L]): F[A] = tell.tuple(t)
}

final class TellOps[L](val e: L) extends AnyVal {
  def tell[F[_]](implicit tell: Tell[F, L]): F[Unit] = tell.tell(e)
}

object tell extends TellSyntax
