package cats
package mtl

/**
  * `FunctorListen[F, L]` is a function `F[A] => F[(A, L)]` which exposes some state
  * that is contained in all `F[A]` values, and can be modified using `tell`.
  *
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
  * `FunctorListen` has one internal law:
  * {{{
  * def listensIsListenThenMap(fa: F[A], f: L => B) = {
  *   listens(fa)(f) <-> listen(fa).map { case (a, l) => (a, f(l)) }
  * }
  * }}}
  */
trait FunctorListen[F[_], L] extends Serializable {
  val tell: FunctorTell[F, L]

  def listen[A](fa: F[A]): F[(A, L)]

  def listens[A, B](fa: F[A])(f: L => B): F[(A, B)]
}

object FunctorListen {
  def apply[F[_], L](implicit listen: FunctorListen[F, L]): FunctorListen[F, L] = listen
  def listen[F[_], L, A](fa: F[A])(implicit ev: FunctorListen[F, L]): F[(A, L)] = ev.listen(fa)
  def listens[F[_], L, A, B](fa: F[A])(f: L => B)(implicit ev: FunctorListen[F, L]): F[(A, B)] = ev.listens(fa)(f)
}

trait DefaultFunctorListen[F[_], L] extends FunctorListen[F, L] {
  def listens[A, B](fa: F[A])(f: L => B): F[(A, B)] = tell.functor.map(listen[A](fa)) {
    case (a, l) => (a, f(l))
  }
}
