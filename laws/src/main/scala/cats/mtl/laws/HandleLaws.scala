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

trait HandleLaws[F[_], E] extends RaiseLaws[F, E] {

  implicit def handleInstance: Handle[F, E]
  implicit def applicativeInstance: Applicative[F] = handleInstance.applicative

  // external laws:
  def raiseAndHandleWithIsFunctionApplication[A](e: E, f: E => F[A]): IsEq[F[A]] =
    handleInstance.handleWith(handleInstance.raise[E, A](e))(f) <-> f(e)

  def raiseAndHandleIsPure[A](e: E, f: E => A): IsEq[F[A]] =
    handleInstance.handle(handleInstance.raise[E, A](e))(f) <-> applicativeInstance.pure(f(e))

  def handleWithPureIsPure[A](a: A, f: E => F[A]): IsEq[F[A]] =
    handleInstance.handleWith(applicativeInstance.pure(a))(f) <-> applicativeInstance.pure(a)

  def handlePureIsPure[A](a: A, f: E => A): IsEq[F[A]] =
    handleInstance.handle(applicativeInstance.pure(a))(f) <-> applicativeInstance.pure(a)

  def raiseAttemptIsPureLeft(e: E): IsEq[F[Either[E, Unit]]] =
    handleInstance.attempt(handleInstance.raise[E, Unit](e)) <-> applicativeInstance.pure(
      Left(e))

  def pureAttemptIsPureRight[A](a: A): IsEq[F[Either[E, A]]] =
    handleInstance.attempt(applicativeInstance.pure(a)) <-> applicativeInstance.pure(Right(a))

  // internal laws:
  def catchNonFatalDefault[A](a: A, f: Throwable => E): IsEq[F[A]] =
    handleInstance.catchNonFatal(a)(f) <-> (try applicativeInstance.pure(a)
    catch {
      case NonFatal(ex) => handleInstance.raise(f(ex))
    })

}

object HandleLaws {
  def apply[F[_], E](implicit instance0: Handle[F, E]): HandleLaws[F, E] = {
    new HandleLaws[F, E] {
      val handleInstance: Handle[F, E] = instance0
      override val raiseInstance: Raise[F, E] = instance0
    }
  }
}
