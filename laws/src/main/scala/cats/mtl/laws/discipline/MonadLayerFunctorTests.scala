package cats
package mtl
package laws
package discipline

import org.scalacheck.Arbitrary
import org.scalacheck.Prop.{forAll => ∀}

trait MonadLayerFunctorTests[M[_], Inner[_]] extends MonadLayerTests[M, Inner] with ApplicativeLayerFunctorTests[M, Inner] {
  implicit val monadLayerFunctorInstance: MonadLayerFunctor[M, Inner]
  override def laws: MonadLayerFunctorLaws[M, Inner] = MonadLayerFunctorLaws[M, Inner]

  def monadLayerFunctor[A: Arbitrary, B](implicit
                                         ArbFA: Arbitrary[Inner[A]],
                                         ArbFABS: Arbitrary[Inner[A => B]],
                                         ArbFAB: Arbitrary[A => Inner[B]],
                                         ArbFun: Arbitrary[Inner ~> Inner],
                                         EqMA: Eq[M[A]],
                                         EqMB: Eq[M[B]]
                                 ): RuleSet = {
    new DefaultRuleSet(
      name = "monadLayerFunctor",
      parent = Some(applicativeLayer[A, B]),
      "layer respects flatMap" -> ∀(laws.layerRespectsFlatMap[A, B](_: Inner[A])(_: A => Inner[B])),
    )
  }
}

object MonadLayerFunctorTests {
  def apply[M[_], Inner[_]](implicit instance: MonadLayerFunctor[M, Inner]): MonadLayerFunctorTests[M, Inner] =
    new MonadLayerFunctorTests[M, Inner] {
      override val monadLayerInstance: MonadLayer[M, Inner] = instance
      override val monadLayerFunctorInstance: MonadLayerFunctor[M, Inner] = instance
      override val applicativeLayerInstance: ApplicativeLayer[M, Inner] = instance
      override val applicativeLayerFunctorInstance: ApplicativeLayerFunctor[M, Inner] = instance
      override val functorLayerFunctorInstance: FunctorLayerFunctor[M, Inner] = instance
      override val functorLayer: FunctorLayer[M, Inner] = instance
    }
}
