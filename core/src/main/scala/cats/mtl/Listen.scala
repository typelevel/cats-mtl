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

import cats.data.{EitherT, StateT, IorT, Kleisli, OptionT, ReaderWriterStateT => RWST, WriterT}
import cats.implicits._
import cats.data.Ior

import scala.annotation.implicitNotFound

/**
  * `Listen[F, L]` is a function `F[A] => F[(A, L)]` which exposes some state
  * that is contained in all `F[A]` values, and can be modified using `tell`.
  *
 * `Listen` has two external laws:
  * {{{
  * def listenRespectsTell(l: L) = {
  *   listen(tell(l)) <-> tell(l).as(((), l))
  * }
  *
 * def listenAddsNoEffects(fa: F[A]) = {
  *   listen(fa).map(_._1) <-> fa
  * }
  * }}}
  *
 * `Listen` has one internal law:
  * {{{
  * def listensIsListenThenMap(fa: F[A], f: L => B) = {
  *   listens(fa)(f) <-> listen(fa).map { case (a, l) => (a, f(l)) }
  * }
  * }}}
  */
@implicitNotFound(
  "Could not find an implicit instance of Listen[${F}, ${L}]. If you wish\nto capture side-channel output of type ${L} at this location, you may want\nto construct a value of type WriterT for this call-site, rather than ${F}.\nAn example type:\n\n  WriterT[${F}, ${L}, *]\n\nOne use-case for this would be if ${L} represents an accumulation of values\nwhich are produced by this function *in addition to* its normal results.\nThis can be used to implement some forms of pure logging.\n\nIf you do not wish to capture a side-channel of type ${L} at this location,\nyou should add an implicit parameter of this type to your function. For\nexample:\n\n  (implicit flisten: Listen[${F}, ${L}}])\n")
trait Listen[F[_], L] extends Tell[F, L] with Serializable {

  def listen[A](fa: F[A]): F[(A, L)]

  def listens[A, B](fa: F[A])(f: L => B): F[(A, B)] =
    functor.map(listen[A](fa)) {
      case (a, l) => (a, f(l))
    }
}

private[mtl] abstract class ListenWriterT[F[_]: Applicative, L]
    extends Listen[WriterT[F, L, *], L] {
  def functor = Functor[WriterT[F, L, *]]
  def tell(l: L) = WriterT.tell[F, L](l)
  def listen[A](fa: WriterT[F, L, A]) = fa.listen
}

private[mtl] abstract class ListenEitherT[F[_]: Functor, E, L](implicit F: Listen[F, L])
    extends Listen[EitherT[F, E, *], L] {
  def functor = Functor[EitherT[F, E, *]]
  def tell(l: L) = EitherT.liftF(F.tell(l))
  def listen[A](fa: EitherT[F, E, A]) =
    EitherT(F.listen(fa.value) map { case (e, l) => e.tupleRight(l) })
}

private[mtl] abstract class ListenKleisli[F[_]: Functor, R, L](implicit F: Listen[F, L])
    extends Listen[Kleisli[F, R, *], L] {
  def functor = Functor[Kleisli[F, R, *]]
  def tell(l: L) = Kleisli.liftF(F.tell(l))
  def listen[A](fa: Kleisli[F, R, A]) =
    Kleisli(a => F.listen(fa.run(a)))
}

private[mtl] abstract class ListenStateT[F[_]: Applicative, S, L](implicit F: Listen[F, L])
    extends Listen[StateT[F, S, *], L] {
  def functor = Functor[StateT[F, S, *]]
  def tell(l: L) = StateT.liftF(F.tell(l))
  def listen[A](fa: StateT[F, S, A]) =
    StateT applyF {
      fa.runF map { f => (s: S) =>
        F.listen(f(s)) map {
          case ((s, a), l) =>
            (s, (a, l))
        }
      }
    }
}

private[mtl] abstract class ListenOptionT[F[_]: Functor, L](implicit F: Listen[F, L])
    extends Listen[OptionT[F, *], L] {
  def functor = Functor[OptionT[F, *]]
  def tell(l: L) = OptionT.liftF(F.tell(l))
  def listen[A](fa: OptionT[F, A]) =
    OptionT(F.listen(fa.value) map { case (opt, l) => opt.tupleRight(l) })
}

private[mtl] abstract class ListenIorT[F[_]: Applicative, E, L](implicit F: Listen[F, L])
    extends Listen[IorT[F, E, *], L] {
  def functor = Functor[IorT[F, E, *]]
  def tell(l: L) = IorT.liftF(F.tell(l))
  def listen[A](fa: IorT[F, E, A]): IorT[F, E, (A, L)] =
    IorT(F.listen(fa.value).map {
      case (Ior.Both(e, a), l) => Ior.both(e, (a, l))
      case (Ior.Left(e), _) => Ior.left(e)
      case (Ior.Right(a), l) => Ior.right((a, l))
    })
}

private[mtl] abstract class ListenRWST[F[_]: Applicative, E, L, S]
    extends Listen[RWST[F, E, L, S, *], L] {
  def functor = Functor[RWST[F, E, L, S, *]]
  def tell(l: L) = RWST.tell(l)
  def listen[A](fa: RWST[F, E, L, S, A]) =
    RWST applyF {
      fa.runF map { f => (e: E, s: S) =>
        f(e, s) map {
          case (l, s, a) => (l, s, (a, l))
        }
      }
    }
}

private[mtl] abstract class ListenInductiveWriterT[F[_]: Applicative, L0: Monoid, L](
    implicit F: Listen[F, L])
    extends Listen[WriterT[F, L0, *], L] {
  def functor = Functor[WriterT[F, L0, *]]
  def tell(l: L) = WriterT.liftF(F.tell(l))
  def listen[A](fa: WriterT[F, L0, A]) =
    WriterT(F.listen(fa.run) map { case ((l0, a), l) => (l0, (a, l)) })
}

private[mtl] abstract class ListenInductiveRWST[F[_]: Applicative, E, L0: Monoid, L, S](
    implicit F: Listen[F, L])
    extends Listen[RWST[F, E, L0, S, *], L] {
  def functor = Functor[RWST[F, E, L0, S, *]]
  def tell(l: L) =
    RWST.liftF[F, E, L0, S, Unit](F.tell(l))

  def listen[A](fa: RWST[F, E, L0, S, A]) =
    RWST applyF {
      fa.runF map { f => (e: E, s: S) =>
        F.listen(f(e, s)) map {
          case ((l0, s, a), l) =>
            (l0, s, (a, l))
        }
      }
    }
}

private[mtl] trait LowPriorityListenInstances {

  implicit def inductiveListenForWriterT[F[_]: Applicative, L, L0: Monoid](
      implicit F: Listen[F, L]): Listen[WriterT[F, L0, *], L] =
    new ListenInductiveWriterT[F, L0, L] {}

  implicit def inductiveListenForRWST[F[_]: Applicative, E, L0: Monoid, S, L](
      implicit F: Listen[F, L]): Listen[RWST[F, E, L0, S, *], L] =
    new ListenInductiveRWST[F, E, L0, L, S] {}
}

private[mtl] trait ListenInstances
    extends LowPriorityListenInstances
    with LowPriorityListenInstancesCompat {

  implicit def listenForEitherT[F[_]: Functor, E, L](
      implicit F: Listen[F, L]): Listen[EitherT[F, E, *], L] =
    new ListenEitherT[F, E, L] {}

  implicit def listenForKleisli[F[_]: Functor, R, L](
      implicit F: Listen[F, L]): Listen[Kleisli[F, R, *], L] =
    new ListenKleisli[F, R, L] {}

  implicit def listenForOptionT[F[_]: Functor, L](
      implicit F: Listen[F, L]): Listen[OptionT[F, *], L] =
    new ListenOptionT[F, L] {}

  implicit def listenForStateT[F[_]: Applicative, S, L](
      implicit F: Listen[F, L]): Listen[StateT[F, S, *], L] =
    new ListenStateT[F, S, L] {}

  implicit def baseListenForWriterT[F[_]: Applicative, L]: Listen[WriterT[F, L, *], L] =
    new ListenWriterT[F, L] {}

  implicit def listenForIorT[F[_]: Applicative, K, L](
      implicit F: Listen[F, K]): Listen[IorT[F, L, *], K] =
    new ListenIorT[F, L, K] {}

  implicit def baseListenForRWST[F[_]: Applicative, E, L, S]: Listen[RWST[F, E, L, S, *], L] =
    new ListenRWST[F, E, L, S] {}
}

object Listen extends ListenInstances {
  def apply[F[_], L](implicit listen: Listen[F, L]): Listen[F, L] = listen
  def listen[F[_], L, A](fa: F[A])(implicit ev: Listen[F, L]): F[(A, L)] = ev.listen(fa)
  def listens[F[_], L, A, B](fa: F[A])(f: L => B)(implicit ev: Listen[F, L]): F[(A, B)] =
    ev.listens(fa)(f)
}
