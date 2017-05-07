package cats
package mtl

@typeclass trait State[F, S] {
  val monad: Monad[F]

}

object State {

}
