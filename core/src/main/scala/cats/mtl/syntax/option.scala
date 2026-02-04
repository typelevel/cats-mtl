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

object option extends OptionSyntax

private[mtl] trait OptionSyntax {
  implicit def catsMtsSyntaxToOptionOps[A](oa: Option[A]): OptionOps[A] = new OptionOps[A](oa)
}

final class OptionOps[A] private[mtl] (private val self: Option[A]) extends AnyVal {
  import OptionOps.*

  /**
   * Lifts `Option[A]` to `F[A]` as long as there's `Raise[F, E]` in the scope and `F` is an
   * `Applicative`, with `E` used as the error value when the `Option` is empty.
   *
   * @note
   *   method `.rescue` in the example requires `ApplicativeError[F, E]` instance.
   *
   * @example
   *   (Scala 3)
   *   {{{
   *   import scala.util.*
   *   import cats.mtl.*, syntax.option.*
   *
   *   case class MyErr(err: String) extends Exception(err)
   *
   *   val res1 =
   *     Handle.allow:
   *       Some(123).liftTo[Try]("OOPS")
   *     .rescue: err =>
   *       Failure(MyErr(err))
   *
   *   assertEquals(res1, Success(123))
   *
   *   val res2 =
   *     Handle.allow:
   *       None.liftTo[Try]("OOPS")
   *     .rescue: err =>
   *       Failure(MyErr(err))
   *
   *   assertEquals(res2, Failure(MyErr("OOPS")))
   *   }}}
   */
  def liftTo[F[_]]: LiftToPartiallyApplied[F, A] =
    new LiftToPartiallyApplied(self)

  /**
   * Raises `Option[A]` to `F[Unit]` as an error when present as long as there's `Raise[F, A]`
   * in the scope and `F` is an `Applicative`
   *
   * @note
   *   method `.rescue` in the example requires `ApplicativeError[F, E]` instance.
   *
   * @example
   *   (Scala 3)
   *   {{{
   *   import scala.util.*
   *   import cats.mtl.*, syntax.option.*
   *
   *   case class MyErr(err: String) extends Exception(err)
   *
   *   val res1 =
   *     Handle.allow:
   *       Some("OOPS").raiseTo[Try]
   *     .rescue: err =>
   *       Failure(MyErr(err))
   *
   *   assertEquals(res1, Failure(MyErr("OOPS")))
   *
   *   val res2 =
   *     Handle.allow:
   *       (None: Option[String]).raiseTo[Try]
   *     .rescue: err =>
   *       Failure(MyErr(err))
   *
   *   assertEquals(res2, Success(()))
   *   }}}
   */
  def raiseTo[F[_]](implicit F: Applicative[F], raise: Raise[F, A]): F[Unit] =
    self.fold(F.unit)(raise.raise)
}

object OptionOps {
  final class LiftToPartiallyApplied[F[_], A](private val self: Option[A]) extends AnyVal {
    def apply[E](ifEmpty: => E)(implicit F: Applicative[F], raise: Raise[F, E]): F[A] =
      raise.fromOption(self)(ifEmpty)
  }
}
