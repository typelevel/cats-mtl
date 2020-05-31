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

/**
  * `FunctorListen[F, L]` is a function `F[A] => F[(A, L)]` which exposes some state
  * that is contained in all `F[A]` values, and can be modified using `tell`.
  *
 * `FunctorListen` has two external laws:
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
 * `FunctorListen` has one internal law:
  * {{{
  * def listensIsListenThenMap(fa: F[A], f: L => B) = {
  *   listens(fa)(f) <-> listen(fa).map { case (a, l) => (a, f(l)) }
  * }
  * }}}
  */
trait FunctorListen[F[_], L] extends FunctorTell[F, L] with Serializable {

  def listen[A](fa: F[A]): F[(A, L)]

  def listens[A, B](fa: F[A])(f: L => B): F[(A, B)] =
    functor.map(listen[A](fa)) {
      case (a, l) => (a, f(l))
    }
}

private[mtl] abstract class FunctorListenWriterT[F[_]: Applicative, L]
    extends FunctorListen[WriterT[F, L, *], L] {
  def functor = Functor[WriterT[F, L, *]]
  def tell(l: L) = WriterT.tell[F, L](l)
  def listen[A](fa: WriterT[F, L, A]) = fa.listen
}

private[mtl] abstract class FunctorListenEitherT[F[_]: Functor, E, L](
    implicit F: FunctorListen[F, L])
    extends FunctorListen[EitherT[F, E, *], L] {
  def functor = Functor[EitherT[F, E, *]]
  def tell(l: L) = EitherT.liftF(F.tell(l))
  def listen[A](fa: EitherT[F, E, A]) =
    EitherT(F.listen(fa.value) map { case (e, l) => e.tupleRight(l) })
}

private[mtl] abstract class FunctorListenKleisli[F[_]: Functor, R, L](
    implicit F: FunctorListen[F, L])
    extends FunctorListen[Kleisli[F, R, *], L] {
  def functor = Functor[Kleisli[F, R, *]]
  def tell(l: L) = Kleisli.liftF(F.tell(l))
  def listen[A](fa: Kleisli[F, R, A]) =
    Kleisli(a => F.listen(fa.run(a)))
}

private[mtl] abstract class FunctorListenStateT[F[_]: Applicative, S, L](
    implicit F: FunctorListen[F, L])
    extends FunctorListen[StateT[F, S, *], L] {
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

private[mtl] abstract class FunctorListenOptionT[F[_]: Functor, L](
    implicit F: FunctorListen[F, L])
    extends FunctorListen[OptionT[F, *], L] {
  def functor = Functor[OptionT[F, *]]
  def tell(l: L) = OptionT.liftF(F.tell(l))
  def listen[A](fa: OptionT[F, A]) =
    OptionT(F.listen(fa.value) map { case (opt, l) => opt.tupleRight(l) })
}

private[mtl] abstract class FunctorListenIorT[F[_]: Applicative, E, L](
    implicit F: FunctorListen[F, L])
    extends FunctorListen[IorT[F, E, *], L] {
  def functor = Functor[IorT[F, E, *]]
  def tell(l: L) = IorT.liftF(F.tell(l))
  def listen[A](fa: IorT[F, E, A]): IorT[F, E, (A, L)] =
    IorT(F.listen(fa.value).map {
      case (Ior.Both(e, a), l) => Ior.both(e, (a, l))
      case (Ior.Left(e), _) => Ior.left(e)
      case (Ior.Right(a), l) => Ior.right((a, l))
    })
}

private[mtl] abstract class FunctorListenRWST[F[_]: Applicative, E, L, S]
    extends FunctorListen[RWST[F, E, L, S, *], L] {
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

private[mtl] abstract class FunctorListenInductiveWriterT[F[_]: Applicative, L0: Monoid, L](
    implicit F: FunctorListen[F, L])
    extends FunctorListen[WriterT[F, L0, *], L] {
  def functor = Functor[WriterT[F, L0, *]]
  def tell(l: L) = WriterT.liftF(F.tell(l))
  def listen[A](fa: WriterT[F, L0, A]) =
    WriterT(F.listen(fa.run) map { case ((l0, a), l) => (l0, (a, l)) })
}

private[mtl] abstract class FunctorListenInductiveRWST[F[_]: Applicative, E, L0: Monoid, L, S](
    implicit F: FunctorListen[F, L])
    extends FunctorListen[RWST[F, E, L0, S, *], L] {
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

private[mtl] trait LowPriorityFunctorListenInstances {

  implicit def inductiveFunctorListenForWriterT[F[_]: Applicative, L, L0: Monoid](
      implicit F: FunctorListen[F, L]): FunctorListen[WriterT[F, L0, *], L] =
    new FunctorListenInductiveWriterT[F, L0, L] {}

  implicit def inductiveFunctorListenForRWST[F[_]: Applicative, E, L0: Monoid, S, L](
      implicit F: FunctorListen[F, L]): FunctorListen[RWST[F, E, L0, S, *], L] =
    new FunctorListenInductiveRWST[F, E, L0, L, S] {}
}

private[mtl] trait FunctorListenInstances
    extends LowPriorityFunctorListenInstances
    with LowPriorityFunctorListenInstancesCompat {

  implicit def functorListenForEitherT[F[_]: Functor, E, L](
      implicit F: FunctorListen[F, L]): FunctorListen[EitherT[F, E, *], L] =
    new FunctorListenEitherT[F, E, L] {}

  implicit def functorListenForKleisli[F[_]: Functor, R, L](
      implicit F: FunctorListen[F, L]): FunctorListen[Kleisli[F, R, *], L] =
    new FunctorListenKleisli[F, R, L] {}

  implicit def functorListenForOptionT[F[_]: Functor, L](
      implicit F: FunctorListen[F, L]): FunctorListen[OptionT[F, *], L] =
    new FunctorListenOptionT[F, L] {}

  implicit def functorListenForStateT[F[_]: Applicative, S, L](
      implicit F: FunctorListen[F, L]): FunctorListen[StateT[F, S, *], L] =
    new FunctorListenStateT[F, S, L] {}

  implicit def baseFunctorListenForWriterT[F[_]: Applicative, L]
      : FunctorListen[WriterT[F, L, *], L] =
    new FunctorListenWriterT[F, L] {}

  implicit def functorListenForIorT[F[_]: Applicative, K, L](
      implicit F: FunctorListen[F, K]): FunctorListen[IorT[F, L, *], K] =
    new FunctorListenIorT[F, L, K] {}

  implicit def baseFunctorListenForRWST[F[_]: Applicative, E, L, S]
      : FunctorListen[RWST[F, E, L, S, *], L] =
    new FunctorListenRWST[F, E, L, S] {}
}

object FunctorListen extends FunctorListenInstances {
  def apply[F[_], L](implicit listen: FunctorListen[F, L]): FunctorListen[F, L] = listen
  def listen[F[_], L, A](fa: F[A])(implicit ev: FunctorListen[F, L]): F[(A, L)] = ev.listen(fa)
  def listens[F[_], L, A, B](fa: F[A])(f: L => B)(implicit ev: FunctorListen[F, L]): F[(A, B)] =
    ev.listens(fa)(f)
}
