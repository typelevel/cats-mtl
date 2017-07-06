package cats
package mtl

/**
  * `FunctorListen` has two external laws:
  * {{{
  * def listenRespectsTell(l: L) = {
  *   listen(tell(l)) <-> tell(l).as(((), l))
  * }
  *
  * def listenAddsNoEffects(fa: F[A]) = {
  *   listen(fa).map(_._1) <-> fa
  * }
  * }}}
  *
  * `FunctorListen` has internal laws:
  * {{{
  * def listensIsListenThenMap(fa: F[A], f: L => B) = {
  *   listens(fa)(f) <-> listen(fa).map { case (a, l) => (a, f(l)) }
  * }
  *
  * def censorIsPassTupled(fa: F[A], f: L => L) = {
  *   censor(fa)(f) <-> pass(fa.map(a => (a, f)))
  * }
  * }}}
  */
trait FunctorListen[F[_], L] extends Serializable {
  val tell: FunctorTell[F, L]

  def listen[A](fa: F[A]): F[(A, L)]

  def pass[A](fa: F[(A, L => L)]): F[A]

  def listens[A, B](fa: F[A])(f: L => B): F[(A, B)]

  def censor[A](fa: F[A])(f: L => L): F[A]
}

object FunctorListen {
  def apply[F[_], L](implicit listen: FunctorListen[F, L]): FunctorListen[F, L] = listen
  def censor[F[_], L, A](fa: F[A])(f: L => L)(implicit ev: FunctorListen[F, L]): F[A] = ev.censor(fa)(f)
  def listen[F[_], L, A](fa: F[A])(implicit ev: FunctorListen[F, L]): F[(A, L)] = ev.listen(fa)
  def listens[F[_], L, A, B](fa: F[A])(f: L => B)(implicit ev: FunctorListen[F, L]): F[(A, B)] = ev.listens(fa)(f)
  def pass[F[_], L, A](fa: F[(A, L => L)])(implicit ev: FunctorListen[F, L]): F[A] = ev.pass(fa)
}
