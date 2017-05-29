package cats
package mtl
package monad

/**
  * Stateful has four external laws:
  * {{{
  * def getThenSetDoesNothing = {
  *   get >> set == pure(())
  * }
  * def setThenGetReturnsSetted(s: S) = {
  *   set(s) >> get == set(s) >> pure(s)
  * }
  * def setThenSetSetsLast(s1: S, s2: S) = {
  *   set(s1) >> set(s2) == set(s2)
  * }
  * def getThenGetGetsOnce = {
  *   get >> get == get
  * }
  * }}}
  *
  * Stateful has one internal law:
  * {{{
  * def modifyIsGetThenSet(f: S => S) = {
  *   modify(f) == (get map f) flatMap set
  * }
  * }}}
  *
  */
trait Stateful[F[_], S] {
  val monad: Monad[F]

  def get: F[S]

  def set(s: S): F[Unit]

  def modify(f: S => S): F[Unit]
}

