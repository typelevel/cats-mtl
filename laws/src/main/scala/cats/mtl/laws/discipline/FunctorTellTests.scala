package cats
package mtl
package laws
package discipline

import org.scalacheck.Prop.{forAll => ∀}
import org.scalacheck.{Arbitrary, Cogen}
import org.typelevel.discipline.Laws

abstract class FunctorTellTests[F[_], L]()(implicit tell: FunctorTell[F, L]) extends Laws {
  def laws: FunctorTellLaws[F, L] = new FunctorTellLaws[F, L]()

  def functorTell[A: Arbitrary](implicit
                                    ArbFA: Arbitrary[F[A]],
                                    ArbL: Arbitrary[L],
                                    CogenA: Cogen[A],
                                    EqFU: Eq[F[Unit]],
                                    EqFA: Eq[F[A]]
                                   ): RuleSet = {
    new DefaultRuleSet(
      name = "functorTell",
      parent = None,
      "tuple is (flip writer)" -> ∀(laws.tupleIsWriterFlipped[A] _),
      "writer is tell and map" -> ∀(laws.writerIsTellAndMap[A] _)
    )
  }

}

