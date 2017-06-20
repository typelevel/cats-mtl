package cats
package mtl
package functor

/**
  * laws:
  * {{{
  *   def transMapIdentity(ma: M[A])(
  * }}}
  */
trait TransFunctor[M[_], Inner[_]] extends LayerFunctor[M, Inner] with Trans[M, Inner] {
  def transMap[A, N[_], NInner[_]](ma: M[A])(trans: Inner ~> NInner)
                                  (implicit mt: Trans.Aux[N, NInner, Outer]): N[A]

  def transInvMap[N[_], NInner[_], A](ma: M[A])
                                     (forward: Inner ~> NInner,
                                      backward: NInner ~> Inner)(implicit other: Trans.Aux[N, NInner, Outer]): N[A] = {
    transMap(ma)(forward)
  }

}

object TransFunctor {

}
