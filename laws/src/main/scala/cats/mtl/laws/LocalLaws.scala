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
package laws

import cats.laws.IsEq
import cats.laws.IsEqArrow

trait LocalLaws[F[_], E] extends AskLaws[F, E] {

  implicit def localInstance: Local[F, E]
  override implicit def applicative = localInstance.applicative

  // external laws:
  def askReflectsLocal(f: E => E): IsEq[F[E]] =
    localInstance.local(localInstance.ask)(f) <-> applicative.map(localInstance.ask)(f)

  def localPureIsPure[A](a: A, f: E => E): IsEq[F[A]] =
    localInstance.local(applicative.pure(a))(f) <-> applicative.pure(a)

  def localDistributesOverAp[A, B](fa: F[A], ff: F[A => B], f: E => E): IsEq[F[B]] =
    localInstance.local(applicative.ap(ff)(fa))(f) <-> applicative.ap(
      localInstance.local(ff)(f))(localInstance.local(fa)(f))

  // internal law:
  def scopeIsLocalConst[A](fa: F[A], e: E): IsEq[F[A]] =
    localInstance.scope(fa)(e) <-> localInstance.local(fa)(_ => e)

}

object LocalLaws {
  def apply[F[_], E](implicit instance0: Local[F, E]): LocalLaws[F, E] = {
    new LocalLaws[F, E] {
      val localInstance: Local[F, E] = instance0
      override val askInstance: Ask[F, E] = instance0
    }
  }
}
