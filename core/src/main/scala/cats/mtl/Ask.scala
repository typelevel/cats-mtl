package cats
package mtl

import cats.data.{ReaderT, StateT}
import cats.mtl.evidence._
import cats.syntax.functor._

trait Ask[F[_], E] {
  implicit val monad: Monad[F]

  type N <: Nat

  def ask: F[E]

  def reader[A](f: E => A): F[A] =
    ask.map(f)
}

object Ask {

  type Aux[N0 <: Nat, F[_], E] = Ask[F, E] {type N = N0}

  def ask[F[_], E](implicit ask: Ask[F, E]): F[E] =
    ask.ask

  def askE[E] = new askEPartiallyApplied[E]

  def askF[F[_]] = new askFPartiallyApplied[F]

  def ask[N <: Nat] = new askPartiallyApplied[N]

  final private[mtl] class askEPartiallyApplied[E](val dummy: Boolean = false) extends AnyVal {
    @inline def apply[F[_]]()(implicit ask: Ask[F, E]): F[E] =
      ask.ask
  }

  final private[mtl] class askFPartiallyApplied[F[_]](val dummy: Boolean = false) extends AnyVal {
    @inline def apply[E]()(implicit ask: Ask[F, E]): F[E] =
      ask.ask
  }

  final private[mtl] class askPartiallyApplied[N <: Nat](val dummy: Boolean = false) extends AnyVal {
    @inline def apply[E, F[_]]()(implicit ask: Ask.Aux[N, F, E]): F[E] =
      ask.ask
  }

  def reader[F[_], E, A](fun: E => A)(implicit ask: Ask[F, E]): F[A] =
    ask.reader(fun)


}

