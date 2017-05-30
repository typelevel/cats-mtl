package cats
package mtl
package monad

/**
  * Listening has one external law:
  * {{{
  *   def listenRespectsTell(l: L) = {
  *     listen(tell(l)) == tell(l).map(_ => ((), l))
  *   }
  * }}}
  *
  * Listening has one internal law:
  * {{{
  *   def passIsListenAndTell[A](fa: F[(A, L => L)]) = {
  *     pass(fa) == listen(fa).flatMap { case ((a, lf), l) => tell(lf(l)).map(_ => a) }
  *   }
  * }}}
  */
trait Listening[F[_], L] {
  val tell: Telling[F, L]

  def listen[A](fa: F[A]): F[(A, L)]

  def pass[A](fa: F[(A, L => L)]): F[A]
}

