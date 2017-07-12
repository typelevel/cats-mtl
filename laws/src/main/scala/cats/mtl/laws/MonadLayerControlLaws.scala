package cats
package mtl
package laws

trait MonadLayerControlLaws[M[_], Inner[_]] extends MonadLayerFunctorLaws[M, Inner] {

  implicit val monadLayerControlInstance: MonadLayerControl[M, Inner]

  import monadLayerControlInstance._
  override implicit val inner: Monad[Inner] = monadLayerControlInstance.innerInstance
  override implicit val outer = monadLayerControlInstance.outerInstance
  import cats.syntax.flatMap._

  def layerMapRespectsLayerControl[A](m: M[A], f: Inner ~> Inner): IsEq[M[A]] = {
    layerMapK(m)(f) <-> layerControl(run => f(run(m))).flatMap(restore)
  }
}

object MonadLayerControlLaws {
  def apply[M[_], Inner[_]](implicit monadLayerControlInstance0: MonadLayerControl[M, Inner]
                           ): MonadLayerControlLaws[M, Inner] = {
    new MonadLayerControlLaws[M, Inner] {
      lazy val monadLayerControlInstance: MonadLayerControl[M, Inner] = monadLayerControlInstance0
      lazy val monadLayerFunctorInstance: MonadLayerFunctor[M, Inner] = monadLayerControlInstance0
      lazy val applicativeLayerFunctorInstance: ApplicativeLayerFunctor[M, Inner] = monadLayerControlInstance0
      lazy val functorLayerFunctorInstance: FunctorLayerFunctor[M, Inner] = monadLayerControlInstance0
      lazy val monadLayerInstance: MonadLayer[M, Inner] = monadLayerControlInstance0
      lazy val applicativeLayerInstance: ApplicativeLayer[M, Inner] = monadLayerControlInstance0
      lazy val functorLayerInstance: FunctorLayer[M, Inner] = monadLayerControlInstance0
    }
  }
}
