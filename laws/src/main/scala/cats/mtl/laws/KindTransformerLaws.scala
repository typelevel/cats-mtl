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

import cats.arrow.FunctionK
import cats.laws.IsEq
import cats.laws.IsEqArrow

trait KindTransformerLaws[F[_], G[_]] {
  implicit def ktInstance: KindTransformer[F, G]

  // external law:
  def limitedMapKIdentityIsPure[A](ga: G[A]): IsEq[G[A]] =
    ktInstance.limitedMapK(ga)(FunctionK.id) <-> ga

  // internal laws:
  def liftKThenLimitedMapKIsMapThenLiftK[A](fa: F[A], f: F ~> F): IsEq[G[A]] =
    ktInstance.limitedMapK(ktInstance.liftK(fa))(f) <-> ktInstance.liftK(f(fa))

  def liftFunctionKApplyIsLimitedMapK[A](ga: G[A], f: F ~> F): IsEq[G[A]] =
    ktInstance.liftFunctionK(f)(ga) <-> ktInstance.limitedMapK(ga)(f)
}

object KindTransformerLaws {
  def apply[F[_], G[_]](
      implicit instance0: KindTransformer[F, G]): KindTransformerLaws[F, G] = {
    new KindTransformerLaws[F, G] {
      override val ktInstance = instance0
    }
  }
}
