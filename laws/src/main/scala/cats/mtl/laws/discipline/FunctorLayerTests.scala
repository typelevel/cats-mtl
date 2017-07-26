package cats
package mtl
package laws
package discipline

import org.scalacheck.Prop.{forAll => ∀}
import org.scalacheck.Arbitrary
import org.typelevel.discipline.Laws

trait FunctorLayerTests[M[_], Inner[_]] extends Laws {
  implicit val functorLayer: FunctorLayer[M, Inner]

  def laws: FunctorLayerLaws[M, Inner] = FunctorLayerLaws[M, Inner]

  def functorLayer[A](implicit
                      ArbFA: Arbitrary[Inner[A]],
                      ArbFun: Arbitrary[Inner ~> Inner],
                      EqFU: Eq[M[A]]
                     ): RuleSet = {
    new DefaultRuleSet(
      name = "functorLayer",
      parent = None,
      "map respects layer" -> ∀(laws.mapForwardRespectsLayer[A](_: Inner[A])(_: Inner ~> Inner, _: Inner ~> Inner))
    )
  }

}
