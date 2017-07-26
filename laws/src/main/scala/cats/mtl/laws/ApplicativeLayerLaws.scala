package cats
package mtl
package laws

import cats.syntax.applicative._
import cats.syntax.apply._

trait ApplicativeLayerLaws[M[_], Inner[_]] extends FunctorLayerLaws[M, Inner] {
  implicit val applicativeLayerInstance: ApplicativeLayer[M, Inner]

  import applicativeLayerInstance._

  implicit val outer = outerInstance
  implicit val inner = innerInstance

  // external laws:
  def layerRespectsPure[A](a: A): IsEq[M[A]] = {
    layer(a.pure[Inner]) <-> a.pure[M]
  }

  def layerRespectsAp[A, B](m: Inner[A])(f: Inner[A => B]): IsEq[M[B]] = {
    layer(f).ap(layer(m)) <-> layer(f.ap(m))
  }
}

object ApplicativeLayerLaws {
  def apply[M[_], Inner[_]](implicit instance0: ApplicativeLayer[M, Inner]): ApplicativeLayerLaws[M, Inner] = {
    new ApplicativeLayerLaws[M, Inner] {
      lazy val applicativeLayerInstance: ApplicativeLayer[M, Inner] = instance0
      override lazy val functorLayerInstance: FunctorLayer[M, Inner] = instance0
    }
  }
}
