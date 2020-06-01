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
package syntax

import cats.data.Ior

trait ChronicleSyntax {
  implicit def toChronicleOps[F[_], A](fa: F[A]): ChronicleOps[F, A] =
    new ChronicleOps[F, A](fa)
  implicit def toChronicleIdOps[E](e: E): ChronicleIdOps[E] = new ChronicleIdOps[E](e)
  implicit def toChronicleIorOps[A, E](ior: E Ior A): ChronicleIorOps[A, E] =
    new ChronicleIorOps[A, E](ior)
}

final class ChronicleOps[F[_], A](val fa: F[A]) extends AnyVal {
  def materialize[E](implicit chronicle: Chronicle[F, E]): F[E Ior A] =
    chronicle.materialize(fa)

  def memento[E](implicit chronicle: Chronicle[F, E]): F[Either[E, A]] =
    chronicle.memento(fa)

  def absolve[E](a: => A)(implicit chronicle: Chronicle[F, E]): F[A] =
    chronicle.absolve(fa)(a)

  def condemn[E](implicit chronicle: Chronicle[F, E]): F[A] =
    chronicle.condemn(fa)

  def retcon[E](cc: E => E)(implicit chronicle: Chronicle[F, E]): F[A] =
    chronicle.retcon(fa)(cc)
}

final class ChronicleIdOps[E](val e: E) extends AnyVal {
  def dictate[F[_]](implicit chronicle: Chronicle[F, E]): F[Unit] =
    chronicle.dictate(e)

  def disclose[F[_], A](implicit chronicle: Chronicle[F, E], monoid: Monoid[A]): F[A] =
    chronicle.disclose(e)

  def confess[F[_], A](implicit chronicle: Chronicle[F, E]): F[A] =
    chronicle.confess(e)
}

final class ChronicleIorOps[A, E](val ior: E Ior A) extends AnyVal {
  def chronicle[F[_]](implicit chronicle: Chronicle[F, E]): F[A] =
    chronicle.chronicle(ior)
}

object chronicle extends ChronicleSyntax
