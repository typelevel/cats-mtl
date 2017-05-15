package cats
package mtl

import cats.syntax.functor._

trait Ask[F[_], E] {
  implicit val monad: Monad[F]

  def ask: F[E]

  def reader[A](f: E => A): F[A] =
    ask.map(f)
}

object Ask {

  def ask[F[_], E](implicit ask: Ask[F, E]): F[E] =
    ask.ask

  def askE[E] = new askEPartiallyApplied[E]

  def askF[F[_]] = new askFPartiallyApplied[F]

  final private[mtl] class askEPartiallyApplied[E](val dummy: Boolean = false) extends AnyVal {
    @inline def apply[F[_]]()(implicit ask: Ask[F, E]): F[E] =
      ask.ask
  }

  final private[mtl] class askFPartiallyApplied[F[_]](val dummy: Boolean = false) extends AnyVal {
    @inline def apply[E]()(implicit ask: Ask[F, E]): F[E] =
      ask.ask
  }

  def reader[F[_], E, A](fun: E => A)(implicit ask: Ask[F, E]): F[A] =
    ask.reader(fun)

}

