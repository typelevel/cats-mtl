package cats
package mtl

trait TFunctor[T[_[_], _]] {
  def mapT[F[_], G[_], A](tfa: T[F, A])(trans: F ~> G): T[G, A]
}
