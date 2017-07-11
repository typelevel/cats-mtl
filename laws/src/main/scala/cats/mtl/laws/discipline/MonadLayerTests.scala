package cats
package mtl
package laws
package discipline

import org.scalacheck.Arbitrary
import org.scalacheck.Prop.{forAll => ∀}

trait MonadLayerTests[M[_], Inner[_]] extends ApplicativeLayerTests[M, Inner] {
  implicit val monadLayerInstance: MonadLayer[M, Inner]
  override def laws: MonadLayerLaws[M, Inner] = MonadLayerLaws[M, Inner]

  def monadLayer[A: Arbitrary, B](implicit
                                  ArbFA: Arbitrary[Inner[A]],
                                  ArbFABS: Arbitrary[A => Inner[B]],
                                  ArbFAB: Arbitrary[Inner[A => B]],
                                  ArbFun: Arbitrary[Inner ~> Inner],
                                  EqMA: Eq[M[A]],
                                  EqMB: Eq[M[B]]): RuleSet = {
    new DefaultRuleSet(
      name = "monadLayer",
      parent = Some(applicativeLayer[A, B]),
      "layer respects flatMap" -> ∀(laws.layerRespectsFlatMap[A, B](_: Inner[A])(_: A => Inner[B]))
    )
  }
}

object MonadLayerTests {
  def apply[M[_], Inner[_]](implicit monadLayerInstance0: MonadLayer[M, Inner]): MonadLayerTests[M, Inner] =
    new MonadLayerTests[M, Inner] {
      override implicit val monadLayerInstance: MonadLayer[M, Inner] = monadLayerInstance0
      override implicit val applicativeLayerInstance: ApplicativeLayer[M, Inner] = monadLayerInstance0
      override implicit val functorLayer: FunctorLayer[M, Inner] = monadLayerInstance0
    }
}
