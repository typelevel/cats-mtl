package cats
package mtl
package laws
package discipline

import org.scalacheck.Prop.{forAll => ∀}
import org.scalacheck.{Arbitrary, Cogen}

class ApplicativeLocalTests[F[_], E]()(implicit local: ApplicativeLocal[F, E]) extends ApplicativeAskTests()(local.ask) {
  override def laws: ApplicativeLocalLaws[F, E] = new ApplicativeLocalLaws[F, E]()

  def applicativeLocal[A: Arbitrary](implicit
                                     ArbFA: Arbitrary[F[A]],
                                     ArbE: Arbitrary[E],
                                     CogenA: Cogen[A],
                                     CogenE: Cogen[E],
                                     EqFU: Eq[F[E]],
                                     EqFA: Eq[F[A]]
                                    ): RuleSet = {
    new DefaultRuleSet(
      name = "applicativeLocal",
      parent = Some(applicativeAsk[A]),
      "ask reflects local" -> ∀(laws.askReflectsLocal _),
      "scope is local const" -> ∀(laws.scopeIsLocalConst[A] _)
    )
  }

}

object ApplicativeLocalTests {
  def apply[F[_], E](implicit local: ApplicativeLocal[F, E]): ApplicativeLocalTests[F, E] = new ApplicativeLocalTests[F, E]()(local)
}
