package cats
package mtl
package laws
package discipline

import org.scalacheck.Prop.{forAll => ∀}
import org.scalacheck.{Arbitrary, Cogen}

abstract class FunctorListenTests[F[_], L]()(implicit listen: FunctorListen[F, L]) extends FunctorTellTests[F, L]()(listen.tell) {
  override def laws: FunctorListenLaws[F, L] = new FunctorListenLaws[F, L]

  def functorListen[A: Arbitrary, B: Arbitrary](implicit
                                                    ArbFA: Arbitrary[F[A]],
                                                    ArbL: Arbitrary[L],
                                                    CogenA: Cogen[A],
                                                    CogenL: Cogen[L],
                                                    EqFU: Eq[F[Unit]],
                                                    EqFA: Eq[F[A]],
                                                    EqFAB: Eq[F[(A, B)]],
                                                    EqFUL: Eq[F[(Unit, L)]]
                                                   ): RuleSet = {
    new DefaultRuleSet(
      name = "functorListen",
      parent = Some(functorTell[A]),
      "listen respects tell" -> ∀(laws.listenRespectsTell _),
      "listen adds no effects" -> ∀(laws.listenAddsNoEffects[A] _),
      "listens is listen then map" -> ∀(laws.listensIsListenThenMap[A, B] _),
      "censor is pass tupled" -> ∀(laws.censorIsPassTupled[A] _)
    )
  }

}

object FunctorListenTests {
  def apply[F[_], L](implicit tell: FunctorListen[F, L]): FunctorListenTests[F, L] = {
    new FunctorListenTests[F, L] {
      override def laws: FunctorListenLaws[F, L] = FunctorListenLaws[F, L]
    }
  }
}
