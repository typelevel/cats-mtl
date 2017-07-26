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
                                         EqMB: Eq[M[B]]): RuleSet = {
    new RuleSet {
      val name = "monadLayerFunctor"
      val parents = Seq(applicativeLayer[A, B], monadLayer[A, B])
      val bases = Seq.empty
      val props = Seq("layer respects flatMap" -> ∀(laws.layerRespectsFlatMap[A, B](_: Inner[A])(_: A => Inner[B])))
    }
  }
}

