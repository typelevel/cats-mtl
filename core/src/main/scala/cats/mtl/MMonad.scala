package cats
package mtl

trait MMonad[T[_[_], _]] {
  def hoist[F[_], G[_], A](input: T[F, A])(trans: F ~> G): T[G, A]
  def ffmap[F[_], A, B](input: T[F, A])(f: A => B)(implicit F: Functor[F]): T[F, B]
  def embed[M[_], N[_], A](trans: M ~> CurryT[T, N]#l)(tma: T[M, A]): T[M, A]
  def low[M[_], A, B](tma: T[M, A])(trans: M[A] => T[M, B]): T[M, B]
}

