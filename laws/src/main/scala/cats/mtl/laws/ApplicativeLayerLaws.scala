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
