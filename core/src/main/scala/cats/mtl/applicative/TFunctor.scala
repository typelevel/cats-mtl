package cats
package mtl
package applicative

trait TFunctor[T[_[_], _]] extends functor.TFunctor[T] {
  def instanceA[F[_]: Applicative]: Applicative[CurryT[T, F]#l]
  def mapT[F[_], G[_], A](tfa: T[F, A])(trans: F ~> G): T[G, A]
}

