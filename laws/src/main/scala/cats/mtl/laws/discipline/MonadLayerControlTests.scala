package cats
package mtl
package laws
package discipline

import org.scalacheck.Prop.{forAll => ∀}
import org.scalacheck.Arbitrary
import org.typelevel.discipline.Laws

trait MonadLayerControlTests[M[_], Inner[_]] extends MonadLayerFunctorTests[M, Inner] {
  implicit val monadLayerControlInstance: MonadLayerControl[M, Inner]

  override def laws: MonadLayerControlLaws[M, Inner] = MonadLayerControlLaws[M, Inner]

  def monadLayerControl[A: Arbitrary, B](implicit
                                         ArbMA: Arbitrary[M[A]],
                                         ArbFA: Arbitrary[Inner[A]],
                                         ArbFABS: Arbitrary[Inner[A => B]],
                                         ArbFAB: Arbitrary[A => Inner[B]],
                                         ArbFun: Arbitrary[Inner ~> Inner],
                                         EqMA: Eq[M[A]],
                                         EqMB: Eq[M[B]]
                                        ): RuleSet = {
    new RuleSet {
      val name = "monadLayerControl"
      val parents = Seq(monadLayerFunctor[A, B])
      val props = Seq(
        "layerMapK respects layerControl" -> ∀(laws.layerMapRespectsLayerControl[A] _)
      )

      def bases: Seq[(String, Laws#RuleSet)] = Seq.empty
    }
  }

}

object MonadLayerControlTests {
  def apply[M[_], Inner[_]](implicit instance0: MonadLayerControl[M, Inner]): MonadLayerControlTests[M, Inner] = {
    new MonadLayerControlTests[M, Inner] with Laws {
      lazy val monadLayerControlInstance: MonadLayerControl[M, Inner] = instance0
      lazy val monadLayerFunctorInstance: MonadLayerFunctor[M, Inner] = instance0
      lazy val applicativeLayerFunctorInstance: ApplicativeLayerFunctor[M, Inner] = instance0
      lazy val functorLayerFunctorInstance: FunctorLayerFunctor[M, Inner] = instance0
      lazy val monadLayerInstance: MonadLayer[M, Inner] = instance0
      lazy val applicativeLayerInstance: ApplicativeLayer[M, Inner] = instance0
      lazy val functorLayer: FunctorLayer[M, Inner] = instance0
    }
  }
}
