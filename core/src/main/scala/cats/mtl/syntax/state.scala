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

trait StateSyntax {
  implicit def catsMtlSyntaxToSetOps[S](e: S): SetOps[S] = new SetOps(e)
  implicit def catsMtlSyntaxToModifyOps[S](f: S => S): ModifyOps[S] = new ModifyOps(f)

  @deprecated("use catsMtlSyntaxToSetOps", "1.7.0")
  def toSetOps[S](e: S): SetOps[S] =
    catsMtlSyntaxToSetOps(e)
  @deprecated("use catsMtlSyntaxToModifyOps", "1.7.0")
  def toModifyOps[S](f: S => S): ModifyOps[S] = catsMtlSyntaxToModifyOps(f)
}

final class SetOps[S](val s: S) extends AnyVal {
  def set[F[_]](implicit stateful: Stateful[F, S]): F[Unit] = stateful.set(s)
}

final class ModifyOps[S](val f: S => S) extends AnyVal {
  def modify[F[_]](implicit stateful: Stateful[F, S]): F[Unit] = stateful.modify(f)
}

object state extends StateSyntax
