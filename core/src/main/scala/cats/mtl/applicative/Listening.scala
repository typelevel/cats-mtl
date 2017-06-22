package cats
package mtl
package applicative

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
  * Listening has internal laws:
  * {{{
  * def listensIsListenThenMap(fa: F[A], f: L => B) = {
  *   listens(fa)(f) == listen(fa).map { case (a, l) => (f(l), a) }
  * }
  *
  * def censorIsPassTupled(fa: F[A])(f: L => L) = {
  *   censor(fa)(f) == pass(fa.map(a => (a, f)))
  * }
  * }}}
  */
trait Listening[F[_], L] {
  val monad: Monad[F]

  val tell: Telling[F, L]

  def listen[A](fa: F[A]): F[(A, L)]

  def pass[A](fa: F[(A, L => L)]): F[A]

  def listens[A, B](fa: F[A])(f: L => B): F[(B, A)]

  def censor[A](fa: F[A])(f: L => L): F[A]
}

object Listening {
  def listen[F[_], L, A](fa: F[A])(implicit ev: Listening[F, L]): F[(A, L)] = ev.listen(fa)
  def pass[F[_], L, A](fa: F[(A, L => L)])(implicit ev: Listening[F, L]): F[A] = ev.pass(fa)
  def listens[F[_], L, A, B](fa: F[A])(f: L => B)(implicit ev: Listening[F, L]): F[(B, A)] = ev.listens(fa)(f)
  def censor[F[_], L, A](fa: F[A])(f: L => L)(implicit ev: Listening[F, L]): F[A] = ev.censor(fa)(f)
}