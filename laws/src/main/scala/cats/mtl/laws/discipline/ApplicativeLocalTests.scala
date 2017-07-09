package cats
package mtl
package laws
package discipline

import org.scalacheck.Prop.{forAll => ∀}
import org.scalacheck.{Arbitrary, Cogen}

trait ApplicativeLocalTests[F[_], E] extends ApplicativeAskTests[F, E] {
  implicit val localInstance: ApplicativeLocal[F, E]
  override def laws: ApplicativeLocalLaws[F, E] = ApplicativeLocalLaws[F, E]

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
  def apply[F[_], E](implicit instance0: ApplicativeLocal[F, E]): ApplicativeLocalTests[F, E] =
    new ApplicativeLocalTests[F, E] {
      override lazy val localInstance: ApplicativeLocal[F, E] = instance0
      override lazy val askInstance: ApplicativeAsk[F, E] = localInstance.ask
    }
}
