package cats
package mtl
package laws

import cats.laws.IsEq
import cats.laws.IsEqArrow
import cats.mtl.lifting._

trait MonadLayerControlLaws[M[_], Inner[_], State0[_]] extends MonadLayerFunctorLaws[M, Inner] {

  implicit val monadLayerControlInstance: MonadLayerControl.Aux[M, Inner, State0]

  import monadLayerControlInstance._

  override implicit val inner: Monad[Inner] = monadLayerControlInstance.innerInstance
  override implicit val outer: Monad[M] = monadLayerControlInstance.outerInstance

  import cats.syntax.flatMap._
  import cats.syntax.applicative._
  import cats.syntax.functor._

  // external laws:
  def distributionLaw[A](nt: State0 ~> State0, st: State0[A]): IsEq[M[A]] = {
    restore(nt(st)) <-> layerControl[State0[A]](_ (restore(st)).map(nt(_))).flatMap(restore)
  }

  def layerControlIdentity[A](ma: M[A]): IsEq[M[A]] = {
    ma <->
      layerControl[Inner[State[A]]] { cps =>
        cps(ma).pure[Inner]
      }.flatMap(layer).flatMap(restore)
  }

  def layerMapRespectsLayerControl[A](m: M[A], f: Inner ~> Inner): IsEq[M[A]] = {
    layerMapK(m)(f) <-> layerControl(run => f(run(m))).flatMap(restore)
  }
}

object MonadLayerControlLaws {
  def apply[M[_], Inner[_], State[_]](implicit monadLayerControlInstance0: MonadLayerControl.Aux[M, Inner, State]
                                     ): MonadLayerControlLaws[M, Inner, State] = {
    new MonadLayerControlLaws[M, Inner, State] {
      lazy val monadLayerControlInstance: MonadLayerControl.Aux[M, Inner, State] = monadLayerControlInstance0
      lazy val monadLayerFunctorInstance: MonadLayerFunctor[M, Inner] = monadLayerControlInstance0
      lazy val applicativeLayerFunctorInstance: ApplicativeLayerFunctor[M, Inner] = monadLayerControlInstance0
      lazy val functorLayerFunctorInstance: FunctorLayerFunctor[M, Inner] = monadLayerControlInstance0
      lazy val monadLayerInstance: MonadLayer[M, Inner] = monadLayerControlInstance0
      lazy val applicativeLayerInstance: ApplicativeLayer[M, Inner] = monadLayerControlInstance0
      lazy val functorLayerInstance: FunctorLayer[M, Inner] = monadLayerControlInstance0
    }
  }
}
