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
package laws

import cats.laws.IsEq
import cats.laws.IsEqArrow

trait LocalLaws[F[_], E] extends AskLaws[F, E] {
  implicit val localInstance: Local[F, E]
  override implicit val applicative = localInstance.applicative

  import localInstance.{local, scope}
  import askInstance.ask
  import applicative._

  // external laws:
  def askReflectsLocal(f: E => E): IsEq[F[E]] =
    local(f)(ask) <-> map(ask)(f)

  def localPureIsPure[A](a: A, f: E => E): IsEq[F[A]] =
    local(f)(pure(a)) <-> pure(a)

  def localDistributesOverAp[A, B](fa: F[A], ff: F[A => B], f: E => E): IsEq[F[B]] =
    local(f)(applicative.ap(ff)(fa)) <-> applicative.ap(local(f)(ff))(local(f)(fa))

  // internal law:
  def scopeIsLocalConst[A](fa: F[A], e: E): IsEq[F[A]] =
    scope(e)(fa) <-> local(_ => e)(fa)

}

object LocalLaws {
  def apply[F[_], E](implicit instance0: Local[F, E]): LocalLaws[F, E] = {
    new LocalLaws[F, E] {
      lazy val localInstance: Local[F, E] = instance0
      override lazy val askInstance: Ask[F, E] = instance0
    }
  }
}
