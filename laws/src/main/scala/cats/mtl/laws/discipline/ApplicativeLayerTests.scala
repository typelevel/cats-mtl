package cats
package mtl
package laws
package discipline

import org.scalacheck.Prop.{forAll => ∀}
import org.scalacheck.Arbitrary
import org.typelevel.discipline.Laws

trait ApplicativeLayerTests[M[_], Inner[_]] extends FunctorLayerTests[M, Inner] {
  implicit val applicativeLayerInstance: ApplicativeLayer[M, Inner]
  override def laws: ApplicativeLayerLaws[M, Inner] = ApplicativeLayerLaws[M, Inner]

  def applicativeLayer[A: Arbitrary, B](implicit
                                                   ArbFA: Arbitrary[Inner[A]],
                                                   ArbFAB: Arbitrary[Inner[A => B]],
                                                   ArbFun: Arbitrary[Inner ~> Inner],
                                                   EqMA: Eq[M[A]],
                                                   EqMB: Eq[M[B]]
                                                  ): RuleSet = {
    new DefaultRuleSet(
      name = "applicativeLayer",
      parent = Some(functorLayer[A]),
      "layer respects ap" -> ∀(laws.layerRespectsAp[A, B](_: Inner[A])(_: Inner[A => B])),
      "layer respects pure" -> ∀(laws.layerRespectsPure[A] _)
    )
  }

}

object ApplicativeLayerTests {
  def apply[M[_], Inner[_]](implicit instance: ApplicativeLayer[M, Inner]): ApplicativeLayerTests[M, Inner] = {
    new ApplicativeLayerTests[M, Inner] with Laws {
      override lazy val applicativeLayerInstance: ApplicativeLayer[M, Inner] = instance
      override lazy val functorLayer: FunctorLayer[M, Inner] = instance
    }
  }
}
