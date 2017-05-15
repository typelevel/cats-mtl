package cats
package mtl

import cats.mtl.evidence._

trait StateN[F, S] {

  val monad: Monad[F]

  type N <: Nat

}

object StateN {

}
