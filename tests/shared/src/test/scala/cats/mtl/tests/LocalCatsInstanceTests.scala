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
package tests

import cats.data._
import cats.laws.discipline.InvariantTests
import cats.laws.discipline.eq._
import org.scalacheck.Arbitrary
import org.scalacheck.Arbitrary._

class LocalCatsInstanceTests extends BaseSuite {
  private implicit def eqKleisli[F[_], A: Arbitrary, B](
      implicit ev: Eq[F[B]]): Eq[Kleisli[F, A, B]] =
    Eq.by((x: Kleisli[F, A, B]) => x.run)

  private implicit def arbLocalReader[F[_]: Monad, AA: Arbitrary]
      : Arbitrary[Local[Kleisli[F, AA, *], AA]] = Arbitrary {
    arbitrary[AA].map { a =>
      new Local[Kleisli[F, AA, *], AA] {
        override def local[A](fa: Kleisli[F, AA, A])(f: AA => AA): Kleisli[F, AA, A] =
          fa.local(f)
        override def applicative: Applicative[Kleisli[F, AA, *]] = implicitly
        override def ask[E2 >: AA]: Kleisli[F, AA, E2] = Kleisli.pure(a)
      }
    }
  }

  private implicit def eqLocal[F[_], A](implicit E: Eq[F[A]]): Eq[Local[F, A]] = Eq.by(_.ask)

  checkAll(
    "Invariant[Local[Kleisli[Option, String, *], *]]",
    InvariantTests[Local[Kleisli[Option, String, *], *]].invariant[String, Int, String])

}
