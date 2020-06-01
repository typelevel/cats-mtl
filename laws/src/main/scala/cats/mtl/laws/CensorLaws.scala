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
import cats.syntax.all._

trait CensorLaws[F[_], L] extends ListenLaws[F, L] {
  implicit def F: Censor[F, L]

  implicit def L: Monoid[L] = F.monoid
  implicit def A: Applicative[F] = F.applicative

  def tellRightProductHomomorphism(l1: L, l2: L): IsEq[F[Unit]] =
    (F.tell(l1) *> F.tell(l2)) <-> F.tell(l1 |+| l2)

  def tellLeftProductHomomorphism(l1: L, l2: L): IsEq[F[Unit]] =
    (F.tell(l1) <* F.tell(l2)) <-> F.tell(l1 |+| l2)

  def censorWithPurIsTellEmpty[A](a: A, f: L => L): IsEq[F[A]] =
    F.censor(A.pure(a))(f) <-> F.tell(f(L.empty)).as(a)

  def clearIsIdempotent[A](fa: F[A]): IsEq[F[A]] =
    F.clear(F.clear(fa)) <-> F.clear(fa)

  def tellAndClearIsPureUnit(l: L): IsEq[F[Unit]] =
    F.clear(F.tell(l)) <-> A.pure(())

}

object CensorLaws {
  def apply[F[_], L](implicit instance0: Censor[F, L]): CensorLaws[F, L] = {
    new CensorLaws[F, L] {
      def F: Censor[F, L] = instance0
    }
  }
}
