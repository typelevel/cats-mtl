package cats
package mtl
package laws
package discipline

import org.scalacheck.Prop.{forAll => ∀}
import org.scalacheck.{Arbitrary, Cogen}
import org.typelevel.discipline.Laws
import cats.kernel.laws.discipline.catsLawsIsEqToProp

trait FunctorTellTests[F[_], L] extends Laws {
  implicit val tellInstance: FunctorTell[F, L]
  def laws: FunctorTellLaws[F, L] = FunctorTellLaws[F, L]

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

object FunctorTellTests {
  def apply[F[_], L](implicit instance0: FunctorTell[F, L]): FunctorTellTests[F, L] = {
    new FunctorTellTests[F, L] with Laws {
      override implicit val tellInstance: FunctorTell[F, L] = instance0
    }
  }
}
