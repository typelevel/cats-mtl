package cats
package mtl
package laws
package discipline

import org.scalacheck.Prop.{forAll => ∀}
import org.scalacheck.{Arbitrary, Cogen, Prop}
import org.typelevel.discipline.Laws

abstract class ApplicativeTellTests[F[_], L]()(implicit tell: FunctorTell[F, L]) extends Laws {
  def laws: FunctorTellLaws[F, L] = new FunctorTellLaws[F, L]()

  def applicativeTell[A: Arbitrary](implicit
                                    ArbFA: Arbitrary[F[A]],
                                    ArbL: Arbitrary[L],
                                    CogenA: Cogen[A],
                                    EqFU: Eq[F[Unit]],
                                    EqFA: Eq[F[A]]
                                   ): RuleSet = {
    new DefaultRuleSet(
      name = "applicativeTell",
      parent = None,
      "tell forms semigroup action" -> ∀(laws.tellTwiceIsTellCombined _),
      "tell forms monoid action" -> Prop.lzy(laws.tellEmptyIsPureUnit),
      "tuple is (flip writer)" -> ∀(laws.tupleIsWriterFlipped[A] _),
      "writer is tell and map" -> ∀(laws.writerIsTellAndMap[A] _)
    )
  }

}

