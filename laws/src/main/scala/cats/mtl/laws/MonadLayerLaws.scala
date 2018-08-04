package cats
package mtl
package laws

import cats.laws.IsEq
import cats.laws.IsEqArrow
import cats.mtl.lifting.{ApplicativeLayer, FunctorLayer, MonadLayer}
import cats.syntax.flatMap._

trait MonadLayerLaws[M[_], Inner[_]] extends ApplicativeLayerLaws[M, Inner] {
  val monadLayerInstance: MonadLayer[M, Inner]
  import monadLayerInstance._
  implicit val innerMonad: Monad[Inner] = monadLayerInstance.innerInstance
  implicit val outerMonad: Monad[M] = monadLayerInstance.outerInstance

  // external law:
  def layerRespectsFlatMap[A, B](m: Inner[A])(f: A => Inner[B]): IsEq[M[B]] = {
    layer(m).flatMap(f andThen layer[B]) <-> layer(m.flatMap(f))
  }
}

object MonadLayerLaws {
  def apply[M[_], Inner[_]](implicit monadLayer: MonadLayer[M, Inner]): MonadLayerLaws[M, Inner] =
    new MonadLayerLaws[M, Inner] {
      override lazy val monadLayerInstance: MonadLayer[M, Inner] = monadLayer
      override lazy val applicativeLayerInstance: ApplicativeLayer[M, Inner] = monadLayer
      override lazy val functorLayerInstance: FunctorLayer[M, Inner] = monadLayer
    }
}
