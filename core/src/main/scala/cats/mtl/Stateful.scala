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

import cats.data.{ReaderWriterStateT => RWST, StateT}

import scala.annotation.implicitNotFound

/**
 * `Stateful[F, S]` is the capability to access and modify a state value
 * from inside the `F[_]` context, using `set(s: S): F[Unit]` and `get: F[S]`.
 *
 * Stateful has four external laws:
 * {{{
 * def getThenSetDoesNothing = {
 *   get >>= set <-> pure(())
 * }
 * def setThenGetReturnsSetted(s: S) = {
 *   set(s) *> get <-> set(s) *> pure(s)
 * }
 * def setThenSetSetsLast(s1: S, s2: S) = {
 *   set(s1) *> set(s2) <-> set(s2)
 * }
 * def getThenGetGetsOnce = {
 *   get *> get <-> get
 * }
 * }}}
 *
 * `Stateful` has two internal law:
 * {{{
 * def modifyIsGetThenSet(f: S => S) = {
 *   modify(f) <-> (inspect(f) flatMap set)
 * }
 *
 * def inspectLaw[A](f: S => A) = {
 *   inspect(f) <-> (get map f)
 * }
 * }}}
 *
 */
@implicitNotFound(
  "Could not find an implicit instance of Stateful[${F}, ${S}]. If you wish\nto ensure that the statefulness of this function is confined within this\nscope, you may want to construct a value of type StateT for this call-site,\nrather than ${F}. An example type:\n\n  StateT[${F}, ${S}, *]\n\nIf you wish the state of ${S} to be threaded *through* this location, rather\nthan being scoped entirely within it, you should add an implicit parameter\nof this type to your function. For example:\n\n  (implicit fstate: Stateful[${F}, ${S}}])\n")
trait Stateful[F[_], S] extends Serializable {
  def monad: Monad[F]

  def inspect[A](f: S => A): F[A] = monad.map(get)(f)

  def modify(f: S => S): F[Unit] = monad.flatMap(inspect(f))(set)

  def get: F[S]

  def set(s: S): F[Unit]
}

private[mtl] trait LowPriorityStatefulInstances {

  implicit def statefulForPartialOrder[F[_], G[_], S](
      implicit liftF: MonadPartialOrder[
        F,
        G
      ], // NB don't make this the *second* parameter; it won't infer
      ms: Stateful[F, S]): Stateful[G, S] =
    new Stateful[G, S] {
      val monad = liftF.monadG
      def get: G[S] = liftF(ms.get)
      def set(s: S): G[Unit] = liftF(ms.set(s))
    }
}

private[mtl] trait StatefulInstances extends LowPriorityStatefulInstances {

  implicit def statefulForStateT[F[_]: Monad, S]: Stateful[StateT[F, S, *], S] =
    new Stateful[StateT[F, S, *], S] {
      val monad = Monad[StateT[F, S, *]]

      def get = StateT.get[F, S]

      def set(s: S) = StateT.set[F, S](s)
    }

  implicit def statefulForRWST[F[_]: Monad, E, L: Monoid, S]: Stateful[RWST[F, E, L, S, *], S] =
    new Stateful[RWST[F, E, L, S, *], S] {
      val monad = Monad[RWST[F, E, L, S, *]]

      def get = RWST.get[F, E, L, S]

      def set(s: S) = RWST.set[F, E, L, S](s)
    }
}

object Stateful extends StatefulInstances {
  def get[F[_], S](implicit ev: Stateful[F, S]): F[S] =
    ev.get

  def set[F[_], S](newState: S)(implicit ev: Stateful[F, S]): F[Unit] =
    ev.set(newState)

  def setF[F[_]]: setFPartiallyApplied[F] = new setFPartiallyApplied[F]

  final private[mtl] class setFPartiallyApplied[F[_]](val dummy: Boolean = false)
      extends AnyVal {
    @inline def apply[E, A](e: E)(implicit state: Stateful[F, E]): F[Unit] =
      state.set(e)
  }

  def modify[F[_], S](f: S => S)(implicit state: Stateful[F, S]): F[Unit] =
    state.modify(f)

  def inspect[F[_], S, A](f: S => A)(implicit state: Stateful[F, S]): F[A] =
    state.inspect(f)

  def apply[F[_], S](implicit stateful: Stateful[F, S]): Stateful[F, S] = stateful
}
