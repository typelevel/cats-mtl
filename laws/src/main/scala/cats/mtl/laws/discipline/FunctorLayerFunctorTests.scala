package cats
package mtl
package laws
package discipline

import org.scalacheck.Prop.{forAll => ∀}
import org.scalacheck.Arbitrary
import org.typelevel.discipline.Laws
import cats.kernel.laws.discipline.catsLawsIsEqToProp

trait FunctorLayerFunctorTests[M[_], Inner[_]] extends FunctorLayerTests[M, Inner] {
  implicit val functorLayerFunctorInstance: FunctorLayerFunctor[M, Inner]
  override def laws: FunctorLayerFunctorLaws[M, Inner] = FunctorLayerFunctorLaws[M, Inner]

  def functorLayerFunctor[A](implicit
                                 ArbMA: Arbitrary[M[A]],
                                 ArbIA: Arbitrary[Inner[A]],
                                 ArbFun: Arbitrary[Inner ~> Inner],
                                 EqFU: Eq[M[A]]
                                ): RuleSet = {
    new DefaultRuleSet(
      name = "functorLayerFunctor",
      parent = Some(functorLayer[A]),
      "layerMapK respects layerImapK" -> ∀(laws.layerMapRespectsLayerImapK[A](_: M[A])(_: Inner ~> Inner, _: Inner ~> Inner))
    )
  }

}

object FunctorLayerFunctorTests {
  def apply[M[_], Inner[_]](implicit instance0: FunctorLayerFunctor[M, Inner]): FunctorLayerFunctorTests[M, Inner] = {
    new FunctorLayerFunctorTests[M, Inner] with Laws {
      override lazy val functorLayerFunctorInstance: FunctorLayerFunctor[M, Inner] = instance0
      override lazy val functorLayer: FunctorLayer[M, Inner] = instance0
    }
  }
}
