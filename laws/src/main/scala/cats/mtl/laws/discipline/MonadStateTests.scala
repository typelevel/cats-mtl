package cats
package mtl
package laws
package discipline

import org.scalacheck.{Arbitrary, Cogen}
import org.scalacheck.Prop.{forAll => ∀}
import org.typelevel.discipline.Laws
import cats.kernel.laws.discipline.catsLawsIsEqToProp

trait MonadStateTests[F[_], S] extends Laws {
  implicit val stateInstance: MonadState[F, S]

  def laws: MonadStateLaws[F, S] = MonadStateLaws[F, S]

  def monadState[A: Arbitrary](implicit
                               ArbFA: Arbitrary[F[A]],
                               ArbS: Arbitrary[S],
                               CogenS: Cogen[S],
                               EqFU: Eq[F[Unit]],
                               EqFS: Eq[F[S]],
                               EqFA: Eq[F[A]]
                              ): RuleSet = {
    new DefaultRuleSet(
      name = "monadState",
      parent = None,
      "get then set has does nothing" -> laws.getThenSetDoesNothing,
      "set then get returns the set value" -> ∀(laws.setThenGetReturnsSet _),
      "set then set sets the last value" -> ∀(laws.setThenSetSetsLast _),
      "get then get gets once" -> laws.getThenGetGetsOnce,
      "modify is get then set" -> ∀(laws.modifyIsGetThenSet _),
      "set is state(unit)" -> ∀(laws.setIsStateUnit _),
      "inspect is state" -> ∀(laws.inpectIsState[A] _),
      "modify is state" -> ∀(laws.modifyIsState _),
      "stateIsGetAndModify" -> ∀(laws.stateIsGetAndModify[A] _)
    )
  }

}

object MonadStateTests {
  def apply[F[_], S](implicit instance0: MonadState[F, S]): MonadStateTests[F, S] = {
    new MonadStateTests[F, S] with Laws {
      override implicit val stateInstance: MonadState[F, S] = instance0
    }
  }
}
