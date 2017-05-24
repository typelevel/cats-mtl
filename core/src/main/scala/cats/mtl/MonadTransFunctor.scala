package cats
package mtl

trait MonadTransFunctor[M[_], Inner[_]] extends MonadLayerFunctor[M, Inner] with MonadTrans[M, Inner] {

  def transMap[A, N[_], NInner[_]](ma: M[A])(trans: Inner ~> NInner)
                                  (implicit mt: MonadTrans.AuxIO[N, NInner, Outer]): N[A]

}
