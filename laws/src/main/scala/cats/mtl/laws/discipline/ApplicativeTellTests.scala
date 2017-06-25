package cats
package mtl
package laws
package discipline

import org.scalacheck.Prop.{forAll => ∀}
import org.scalacheck.{Arbitrary, Cogen, Prop}
import org.typelevel.discipline.Laws

abstract class ApplicativeTellTests[F[_], L] extends Laws {
  def laws: ApplicativeTellLaws[F, L]

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
      "tell forms monoid action" -> (laws.tellEmptyIsPureUnit: Prop),
      "tuple is (flip writer)" -> ∀(laws.tupleIsWriterFlipped[A] _),
      "writer is tell ann map" -> ∀(laws.writerIsTellAndMap[A] _)
    )
  }

}

object ApplicativeTellTests {
  def apply[F[_], L](implicit tell: ApplicativeTell[F, L]): ApplicativeTellTests[F, L] = {
    new ApplicativeTellTests[F, L] {
      def laws: ApplicativeTellLaws[F, L] = ApplicativeTellLaws[F, L]
    }
  }
}

