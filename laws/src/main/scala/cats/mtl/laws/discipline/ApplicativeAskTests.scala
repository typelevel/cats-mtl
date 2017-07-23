package cats
package mtl
package laws
package discipline

import org.scalacheck.Prop.{forAll => ∀}
import org.scalacheck.{Arbitrary, Cogen}
import org.typelevel.discipline.Laws

trait ApplicativeAskTests[F[_], E] extends Laws {
  implicit val askInstance: ApplicativeAsk[F, E]

  def laws: ApplicativeAskLaws[F, E] = ApplicativeAskLaws[F, E]

  def applicativeAsk[A: Arbitrary](implicit
                                   ArbFA: Arbitrary[F[A]],
                                   ArbE: Arbitrary[E],
                                   CogenA: Cogen[A],
                                   CogenE: Cogen[E],
                                   EqFU: Eq[F[E]],
                                   EqFA: Eq[F[A]]
                                  ): RuleSet = {
    new DefaultRuleSet(
      name = "applicativeAsk",
      parent = None,
      "ask adds no effects" -> ∀(laws.askAddsNoEffects[A] _),
      "reader is ask and map" -> ∀(laws.readerIsAskAndMap[A] _)
    )
  }

}

object ApplicativeAskTests {
  def apply[F[_], E](implicit instance: ApplicativeAsk[F, E]): ApplicativeAskTests[F, E] = {
    new ApplicativeAskTests[F, E] {
      val askInstance = instance
    }
  }
}

