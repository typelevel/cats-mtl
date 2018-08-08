package cats
package mtl
package laws
package discipline

import org.scalacheck.Prop.{forAll => ∀}
import org.scalacheck.{Arbitrary, Cogen}
import org.typelevel.discipline.Laws
import cats.kernel.laws.discipline.catsLawsIsEqToProp

trait FunctorRaiseTests[F[_], E] extends Laws {
  implicit val raiseInstance: FunctorRaise[F, E]
  def laws: FunctorRaiseLaws[F, E] = FunctorRaiseLaws[F, E]

  def functorRaise[A: Arbitrary](implicit
                                ArbFA: Arbitrary[F[A]],
                                ArbE: Arbitrary[E],
                                CogenA: Cogen[A],
                                EqFU: Eq[F[Unit]],
                                EqFA: Eq[F[A]],
                                MonadF: Monad[F]
                               ): RuleSet = {
    new DefaultRuleSet(
      name = "functorRaise",
      parent = None,
      "catch non fatal default" -> ∀(laws.catchNonFatalDefault[A] _),
      "ensure default" -> ∀(laws.ensureDefault[A] _)
    )
  }

}

object FunctorRaiseTests {
  def apply[F[_], E](implicit instance0: FunctorRaise[F, E]): FunctorRaiseTests[F, E] = {
    new FunctorRaiseTests[F, E] with Laws {
      override implicit val raiseInstance: FunctorRaise[F, E] = instance0
    }
  }
}
