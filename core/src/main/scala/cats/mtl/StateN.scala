package cats
package mtl

trait StateN[F[_], S] {

  val monad: Monad[F]

}

