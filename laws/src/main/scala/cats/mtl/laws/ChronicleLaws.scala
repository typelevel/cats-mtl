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

import cats.data.Ior
import cats.laws.{IsEq, IsEqArrow}
import cats.syntax.functor._
import cats.syntax.apply._
import cats.syntax.semigroup._

/**
 * Created by Yuval.Itzchakov on 20/07/2018.
 */
trait ChronicleLaws[F[_], E] {
  implicit def chronicleInstance: Chronicle[F, E]
  implicit def monad: Monad[F] = chronicleInstance.monad

  def dictateThenMaterializeIsBoth(e: E): IsEq[F[E Ior Unit]] =
    chronicleInstance.materialize(chronicleInstance.dictate(e)) <-> monad.pure(Ior.Both(e, ()))

  def confessThenMaterializeIsLeft[A](e: E): IsEq[F[E Ior A]] =
    chronicleInstance.materialize[A](chronicleInstance.confess(e)) <-> monad.pure(Ior.Left(e))

  def pureThenMaterializeIsRight[A](a: A): IsEq[F[E Ior A]] =
    chronicleInstance.materialize[A](monad.pure(a)) <-> monad.pure(Ior.Right(a))

  def confessThenAbsolveIsPure[A](a: A, e: E): IsEq[F[A]] =
    chronicleInstance.absolve(chronicleInstance.confess[A](e))(a) <-> monad.pure(a)

  def dictateThenCondemIsConfess[A](e: E): IsEq[F[Unit]] =
    chronicleInstance.condemn(chronicleInstance.dictate(e)) <-> chronicleInstance.confess(e)

  def confessThenMementoIsLeft[A](e: E): IsEq[F[Either[E, A]]] =
    chronicleInstance.memento[A](chronicleInstance.confess(e)) <-> monad.pure(Left(e))

  def dictateThenMementoIsDictateRightUnit(e: E): IsEq[F[Either[E, Unit]]] =
    chronicleInstance.memento(chronicleInstance.dictate(e)) <-> chronicleInstance
      .dictate(e)
      .map(_ => Right(()))

  def confessThenRetconIsConfess[A](f: E => E, e: E): IsEq[F[A]] =
    chronicleInstance.retcon(chronicleInstance.confess[A](e))(f) <-> chronicleInstance
      .confess[A](f(e))

  def dictateThenRetconIsDictate[A](f: E => E, e: E): IsEq[F[Unit]] =
    chronicleInstance.retcon(chronicleInstance.dictate(e))(f) <-> chronicleInstance.dictate(
      f(e))

  def pureThenRetconIsPure[A](f: E => E, a: A): IsEq[F[A]] =
    chronicleInstance.retcon(monad.pure(a))(f) <-> monad.pure(a)

  def dictateSharkDictateIsDictateSemigroup(e0: E, e: E)(
      implicit ev: Semigroup[E]): IsEq[F[Unit]] =
    chronicleInstance.dictate(e0) *> chronicleInstance.dictate(e) <-> chronicleInstance.dictate(
      e0 |+| e)

  def dictateSharkConfessIsConfessSemigroup[A](e0: E, e: E)(
      implicit ev: Semigroup[E]): IsEq[F[A]] =
    chronicleInstance.dictate(e0) *> chronicleInstance.confess[A](e) <-> chronicleInstance
      .confess[A](e0 |+| e)
}

object ChronicleLaws {
  def apply[F[_], E](implicit instance: Chronicle[F, E]): ChronicleLaws[F, E] =
    new ChronicleLaws[F, E] {
      override implicit val chronicleInstance: Chronicle[F, E] = instance
    }
}
