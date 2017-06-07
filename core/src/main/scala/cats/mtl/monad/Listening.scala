package cats
package mtl
package monad

/**
  * Listening has two external laws:
  * {{{
  * def listenRespectsTell(l: L) = {
  *   listen(tell(l)) == tell(l).map(_ => ((), l))
  * }
  *
  * def listenAddsNoEffects(fa: F[A]) = {
  *   listen(fa).map(_._1) == fa
  * }
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

