package cats
package mtl

import cats.data.EitherT
import syntax.applicative._
import simulacrum.typeclass

@typeclass trait Handle[F[_], E] {
  val raise: Raise[F, E]

  def handleError[A](fa: F[A])(handler: E => Option[A]): F[A]
}

object Handle {
  type EitherTC[F[_], E] = {type l[A] = EitherT[F, E, A]}
}
