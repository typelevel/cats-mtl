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

import cats.data.Ior
import cats.laws.{IsEq, IsEqArrow}
import cats.syntax.functor._
import cats.syntax.apply._
import cats.syntax.semigroup._

/**
 * Created by Yuval.Itzchakov on 20/07/2018.
 */
trait ChronicleLaws[F[_], E] {
  implicit val chronicleInstance: Chronicle[F, E]
  implicit val monad: Monad[F] = chronicleInstance.monad

  import chronicleInstance._
  import monad._

  def dictateThenMaterializeIsBoth(e: E): IsEq[F[E Ior Unit]] =
    materialize(dictate(e)) <-> pure(Ior.Both(e, ()))

  def confessThenMaterializeIsLeft[A](e: E): IsEq[F[E Ior A]] =
    materialize[A](confess(e)) <-> pure(Ior.Left(e))

  def pureThenMaterializeIsRight[A](a: A): IsEq[F[E Ior A]] =
    materialize[A](pure(a)) <-> pure(Ior.Right(a))

  def confessThenAbsolveIsPure[A](a: A, e: E): IsEq[F[A]] =
    absolve(confess[A](e))(a) <-> pure(a)

  def dictateThenCondemIsConfess[A](e: E): IsEq[F[Unit]] =
    condemn(dictate(e)) <-> confess(e)

  def confessThenMementoIsLeft[A](e: E): IsEq[F[Either[E, A]]] =
    memento[A](confess(e)) <-> pure(Left(e))

  def dictateThenMementoIsDictateRightUnit(e: E): IsEq[F[Either[E, Unit]]] =
    memento(dictate(e)) <-> dictate(e).map(_ => Right(()))

  def confessThenRetconIsConfess[A](f: E => E, e: E): IsEq[F[A]] =
    retcon(confess[A](e))(f) <-> confess[A](f(e))

  def dictateThenRetconIsDictate[A](f: E => E, e: E): IsEq[F[Unit]] =
    retcon(dictate(e))(f) <-> dictate(f(e))

  def pureThenRetconIsPure[A](f: E => E, a: A): IsEq[F[A]] =
    retcon(pure(a))(f) <-> pure(a)

  def dictateSharkDictateIsDictateSemigroup(e0: E, e: E)(
      implicit ev: Semigroup[E]): IsEq[F[Unit]] =
    dictate(e0) *> dictate(e) <-> dictate(e0 |+| e)

  def dictateSharkConfessIsConfessSemigroup[A](e0: E, e: E)(
      implicit ev: Semigroup[E]): IsEq[F[A]] =
    dictate(e0) *> confess[A](e) <-> confess[A](e0 |+| e)
}

object ChronicleLaws {
  def apply[F[_], E](implicit instance: Chronicle[F, E]): ChronicleLaws[F, E] =
    new ChronicleLaws[F, E] {
      override lazy implicit val chronicleInstance: Chronicle[F, E] = instance
    }
}
