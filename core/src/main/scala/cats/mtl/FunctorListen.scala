package cats
package mtl

import cats.data.{EitherT, StateT, Kleisli, OptionT, ReaderWriterStateT => RWST, WriterT}
import cats.implicits._

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

private[mtl] trait LowPriorityFunctorListenInstances {

  implicit def inductiveFunctorListenForWriterT[F[_]: Applicative, L, L0: Monoid](
      implicit F: FunctorListen[F, L])
      : FunctorListen[WriterT[F, L0, *], L] =
    new FunctorListen[WriterT[F, L0, *], L] {

      val functor = Functor[WriterT[F, L0, *]]

      def tell(l: L) =
        WriterT.liftF(F.tell(l))

      def listen[A](fa: WriterT[F, L0, A]) =
        WriterT(F.listen(fa.run) map { case ((l0, a), l) => (l0, (a, l)) })
    }

  implicit def inductiveFunctorListenForRWST[F[_]: Applicative, E, L0: Monoid, S, L](implicit F: FunctorListen[F, L]): FunctorListen[RWST[F, E, L0, S, *], L] =
    new FunctorListen[RWST[F, E, L0, S, *], L] {

      val functor = Functor[RWST[F, E, L0, S, *]]

      def tell(l: L) =
        RWST.liftF(F.tell(l))

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
}

private[mtl] trait FunctorListenInstances extends LowPriorityFunctorListenInstances {

  implicit def functorListenForEitherT[F[_]: Functor, E, L](implicit F: FunctorListen[F, L]): FunctorListen[EitherT[F, E, *], L] =
    new FunctorListen[EitherT[F, E, *], L] {

      val functor = Functor[EitherT[F, E, *]]

      def tell(l: L) =
        EitherT.liftF(F.tell(l))

      def listen[A](fa: EitherT[F, E, A]) =
        EitherT(F.listen(fa.value) map { case (e, l) => e.tupleRight(l) })
    }

  implicit def functorListenForKleisli[F[_]: Functor, R, L](implicit F: FunctorListen[F, L]): FunctorListen[Kleisli[F, R, *], L] =
    new FunctorListen[Kleisli[F, R, *], L] {

      val functor = Functor[Kleisli[F, R, *]]

      def tell(l: L) =
        Kleisli.liftF(F.tell(l))

      def listen[A](fa: Kleisli[F, R, A]) =
        Kleisli(a => F.listen(fa.run(a)))
    }

  implicit def functorListenForOptionT[F[_]: Functor, L](implicit F: FunctorListen[F, L]): FunctorListen[OptionT[F, *], L] =
    new FunctorListen[OptionT[F, *], L] {

      val functor = Functor[OptionT[F, *]]

      def tell(l: L) =
        OptionT.liftF(F.tell(l))

      def listen[A](fa: OptionT[F, A]) =
        OptionT(F.listen(fa.value) map { case (opt, l) => opt.tupleRight(l) })
    }

  implicit def functorListenForStateT[F[_]: Applicative, S, L](implicit F: FunctorListen[F, L]): FunctorListen[StateT[F, S, *], L] =
    new FunctorListen[StateT[F, S, *], L] {

      val functor = Functor[StateT[F, S, *]]

      def tell(l: L) =
        StateT.liftF(F.tell(l))

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

  implicit def baseFunctorListenForWriterT[F[_]: Applicative, L]: FunctorListen[WriterT[F, L, *], L] =
    new FunctorListen[WriterT[F, L, *], L] {
      val functor = Functor[WriterT[F, L, *]]
      def tell(l: L) = WriterT.tell[F, L](l)
      def listen[A](fa: WriterT[F, L, A]) = fa.listen
    }

  implicit def baseFunctorListenForRWST[F[_]: Applicative, E, L, S]: FunctorListen[RWST[F, E, L, S, *], L] =
    new FunctorListen[RWST[F, E, L, S, *], L] {

      val functor = Functor[RWST[F, E, L, S, *]]

      def tell(l: L) =
        RWST.tell(l)

      def listen[A](fa: RWST[F, E, L, S, A]) =
        RWST applyF {
          fa.runF map { f => (e: E, s: S) =>
            f(e, s) map {
              case (l, s, a) => (l, s, (a, l))
            }
          }
        }
    }
}

object FunctorListen extends FunctorListenInstances {
  def apply[F[_], L](implicit listen: FunctorListen[F, L]): FunctorListen[F, L] = listen
  def listen[F[_], L, A](fa: F[A])(implicit ev: FunctorListen[F, L]): F[(A, L)] = ev.listen(fa)
  def listens[F[_], L, A, B](fa: F[A])(f: L => B)(implicit ev: FunctorListen[F, L]): F[(A, B)] = ev.listens(fa)(f)
}
