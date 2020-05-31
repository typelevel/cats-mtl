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

import scala.util.control.NonFatal

trait ApplicativeHandleLaws[F[_], E] extends FunctorRaiseLaws[F, E] {
  implicit val handleInstance: ApplicativeHandle[F, E]
  implicit val applicativeInstance: Applicative[F] = handleInstance.applicative

  import handleInstance.{attempt, handle, handleWith}
  import raiseInstance.{catchNonFatal, raise}

  import applicativeInstance._

  // external laws:
  def raiseAndHandleWithIsFunctionApplication[A](e: E, f: E => F[A]): IsEq[F[A]] =
    handleWith(raise[A](e))(f) <-> f(e)

  def raiseAndHandleIsPure[A](e: E, f: E => A): IsEq[F[A]] =
    handle(raise[A](e))(f) <-> pure(f(e))

  def handleWithPureIsPure[A](a: A, f: E => F[A]): IsEq[F[A]] =
    handleWith(pure(a))(f) <-> pure(a)

  def handlePureIsPure[A](a: A, f: E => A): IsEq[F[A]] =
    handle(pure(a))(f) <-> pure(a)

  def raiseAttemptIsPureLeft(e: E): IsEq[F[Either[E, Unit]]] =
    attempt(raise[Unit](e)) <-> pure(Left(e))

  def pureAttemptIsPureRight[A](a: A): IsEq[F[Either[E, A]]] =
    attempt(pure(a)) <-> pure(Right(a))

  // internal laws:
  def catchNonFatalDefault[A](a: A, f: Throwable => E): IsEq[F[A]] =
    catchNonFatal(a)(f) <-> (try pure(a)
    catch {
      case NonFatal(ex) => raise(f(ex))
    })

}

object ApplicativeHandleLaws {
  def apply[F[_], E](
      implicit instance0: ApplicativeHandle[F, E]): ApplicativeHandleLaws[F, E] = {
    new ApplicativeHandleLaws[F, E] {
      lazy val handleInstance: ApplicativeHandle[F, E] = instance0
      override lazy val raiseInstance: FunctorRaise[F, E] = instance0
    }
  }
}
