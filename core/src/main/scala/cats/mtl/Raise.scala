package cats
package mtl

import cats.data.EitherT
import evidence._

trait Raise[F[_], E] {
  val monad: Monad[F]

  def raiseError[A](e: E): F[A]
}
