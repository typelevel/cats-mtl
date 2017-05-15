package cats
package mtl

trait MonadTransFunctor[M[_]] extends MonadLayerFunctor[M] with MonadTrans[M] {

  def transMap[A, N[_], NInner[_]](ma: M[A])(trans: Inner ~> NInner): N[A]

}
