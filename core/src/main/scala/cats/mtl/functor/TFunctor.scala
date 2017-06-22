package cats
package mtl
package functor

trait TFunctor[T[_[_], _]] {
  def instanceF[F[_]: Functor]: Functor[CurryT[T, F]#l]
  def mapT[F[_], G[_], A](tfa: T[F, A])(trans: F ~> G): T[G, A]
}

