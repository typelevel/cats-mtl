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

object either extends EitherSyntax

private[mtl] trait EitherSyntax {
  implicit def catsMtsSyntaxToEitherOps[A, B](oa: Either[A, B]): EitherOps[A, B] =
    new EitherOps[A, B](oa)
}

final class EitherOps[A, B] private[mtl] (private val self: Either[A, B]) extends AnyVal {

  /**
   * Lifts `Either[A, B]` to `F[B]` as long as there's `Raise[F, A]` in the scope and `F` is an
   * `Applicative`.
   *
   * @note
   *   method `.rescue` in the example requires `ApplicativeError[F, E]` instance.
   *
   * @example
   *   (Scala 3)
   *   {{{
   *   import scala.util.*
   *   import cats.mtl.*, syntax.either.*
   *
   *   case class MyErr(err: String) extends Exception(err)
   *
   *   val res1 =
   *     Handle.allow:
   *       Right[String, Int](123).liftTo[Try]
   *     .rescue: err =>
   *       Failure(MyErr(err))
   *
   *   assertEquals(res1, Success(123))
   *
   *   val res2 =
   *     Handle.allow:
   *       Left[String, Int]("OOPS").liftTo[Try]
   *     .rescue: err =>
   *       Failure(MyErr(err))
   *
   *   assertEquals(res2, Failure("OOPS"))
   *   }}}
   */
  def liftTo[F[_]](implicit F: Applicative[F], raise: Raise[F, A]): F[B] =
    raise.fromEither(self)
}
