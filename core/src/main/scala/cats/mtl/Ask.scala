package cats
package mtl

import simulacrum.typeclass
import syntax.functor._

@typeclass trait Ask[F[_], E] {
  val monad: Monad[F]
  def ask: F[E]
  def reader[A](f: E => A)(implicit F: Functor[F]): F[A] =
    ask.map(f)
}

object Ask {
  sealed trait AskCanDo[S, Eff]
}