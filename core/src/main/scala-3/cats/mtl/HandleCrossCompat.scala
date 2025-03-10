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

private[mtl] trait HandleCrossCompat { this: Handle.type =>
  import Handle.Submarine

  inline def allow[E]: AdHocSyntaxWired[E] =
    new AdHocSyntaxWired[E]()

  private[mtl] final class AdHocSyntaxWired[E]:
    inline def apply[F[_], A](inline body: Handle[F, E] ?=> F[A]): InnerWired[F, E, A] =
      new InnerWired(body)

  private[mtl] final class InnerWired[F[_], E, A](body: Handle[F, E] ?=> F[A]):
    def rescue(h: E => F[A])(using ApplicativeThrow[F]): F[A] =
      val Marker = new AnyRef

      def inner[B](fb: F[B])(f: E => F[B]): F[B] =
        ApplicativeThrow[F].handleErrorWith(fb):
          case Submarine(e, Marker) => f(e.asInstanceOf[E])
          case t => ApplicativeThrow[F].raiseError(t)

      given Handle[F, E] with
        def applicative = Applicative[F]
        def raise[E2 <: E, B](e: E2): F[B] =
          ApplicativeThrow[F].raiseError(Submarine(e, Marker))
        def handleWith[B](fb: F[B])(f: E => F[B]): F[B] = inner(fb)(f)

      inner(body)(h)
}
