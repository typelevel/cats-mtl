package cats
package mtl
package laws
package discipline

import org.scalacheck.Prop.{forAll => ∀}
import org.scalacheck.Arbitrary
import org.typelevel.discipline.Laws
import cats.kernel.laws.discipline.catsLawsIsEqToProp

trait MonadLayerControlTests[M[_], Inner[_], State[_]] extends MonadLayerFunctorTests[M, Inner] {
  implicit val monadLayerControlInstance: MonadLayerControl.Aux[M, Inner, State]

  override def laws: MonadLayerControlLaws[M, Inner, State] = MonadLayerControlLaws[M, Inner, State]

  def monadLayerControl[A: Arbitrary, B](implicit
                                         ArbMA: Arbitrary[M[A]],
                                         ArbFA: Arbitrary[Inner[A]],
                                         ArbSt: Arbitrary[State[A]],
                                         ArbFABS: Arbitrary[Inner[A => B]],
                                         ArbFAB: Arbitrary[A => Inner[B]],
                                         ArbInT: Arbitrary[Inner ~> Inner],
                                         ArbStT: Arbitrary[State ~> State],
                                         EqMA: Eq[M[A]],
                                         EqMB: Eq[M[B]]
                                        ): RuleSet = {
    new RuleSet {
      val name = "monadLayerControl"
      val parents = Seq(monadLayerFunctor[A, B])
      val props = Seq(
        "layerMapK respects layerControl" -> ∀(laws.layerMapRespectsLayerControl[A] _),
        "distribution law" -> ∀(laws.distributionLaw[A] _),
        "layerControl identity" -> ∀(laws.layerControlIdentity[A] _)
      )

      def bases: Seq[(String, Laws#RuleSet)] = Seq.empty
    }
  }

}

object MonadLayerControlTests {
  def apply[M[_], Inner[_], State[_]](implicit instance0: MonadLayerControl.Aux[M, Inner, State]): MonadLayerControlTests[M, Inner, State] = {
    new MonadLayerControlTests[M, Inner, State] {
      lazy val monadLayerControlInstance: MonadLayerControl.Aux[M, Inner, State] = instance0
      lazy val monadLayerFunctorInstance: MonadLayerFunctor[M, Inner] = instance0
      lazy val applicativeLayerFunctorInstance: ApplicativeLayerFunctor[M, Inner] = instance0
      lazy val functorLayerFunctorInstance: FunctorLayerFunctor[M, Inner] = instance0
      lazy val monadLayerInstance: MonadLayer[M, Inner] = instance0
      lazy val applicativeLayerInstance: ApplicativeLayer[M, Inner] = instance0
      lazy val functorLayer: FunctorLayer[M, Inner] = instance0
    }
  }
}
