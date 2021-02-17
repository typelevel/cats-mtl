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
import cats.syntax.functor._

trait ListenLaws[F[_], L] extends TellLaws[F, L] {
  implicit def F: Listen[F, L]

  // external laws:
  def listenRespectsTell(l: L): IsEq[F[(Unit, L)]] =
    F.listen(F.tell(l)) <-> F.tell(l).as(((), l))

  def listenAddsNoEffects[A](fa: F[A]): IsEq[F[A]] =
    F.listen(fa).map(_._1) <-> fa

  // internal law:
  def listensIsListenThenMap[A, B](fa: F[A], f: L => B): IsEq[F[(A, B)]] =
    F.listens(fa)(f) <-> F.listen(fa).map { case (a, l) => (a, f(l)) }
}

object ListenLaws {
  def apply[F[_], E](implicit instance0: Listen[F, E]): ListenLaws[F, E] = {
    new ListenLaws[F, E] {
      def F: Listen[F, E] = instance0
    }
  }
}
