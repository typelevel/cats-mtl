package cats
package mtl
package laws
package discipline

import cats.data.Ior
import org.scalacheck.Prop.{forAll => ∀}
import org.scalacheck.{Arbitrary, Cogen}
import org.typelevel.discipline.Laws
import cats.kernel.laws.discipline.catsLawsIsEqToProp

trait MonadChronicleTests[F[_], E] extends Laws {
  implicit val chronicleInstance: MonadChronicle[F, E]

  def laws: MonadChronicleLaws[F, E] = MonadChronicleLaws[F, E]

  def monadChronicle[A: Arbitrary](implicit ArbFA: Arbitrary[F[A]],
                                   ArbE: Arbitrary[E],
                                   CogenA: Cogen[A],
                                   CogenE: Cogen[E],
                                   EqFU: Eq[F[E]],
                                   EqFA: Eq[F[A]],
                                   EqFUnit: Eq[F[Unit]],
                                   EqFEIorUnit: Eq[F[E Ior Unit]],
                                   EqFEUnit: Eq[F[Either[E, Unit]]],
                                   EqFIor: Eq[F[E Ior A]],
                                   EqFEither: Eq[F[Either[E, A]]]): RuleSet = {
    new DefaultRuleSet(
      name = "monadChronicle",
      parent = None,
      "confess then absolve is pure" -> ∀(laws.confessThenAbsolveIsPure[A] _),
      "confess then materialize is left" -> ∀(laws.confessThenMaterializeIsLeft[A] _),
      "confess then materialize is le" -> ∀(laws.confessThenMementoIsLeft[A] _),
      "confess then retcon is confess" -> ∀(laws.confessThenRetconIsConfess[A] _),
      "dictate then condem is confess" -> ∀(laws.dictateThenCondemIsConfess[A] _),
      "dictate then materialize is both" -> ∀(laws.dictateThenMaterializeIsBoth _),
      "dictate then memento is dictate right unit" -> ∀(laws.dictateThenMementoIsDictateRightUnit _),
      "dictate then retcon is dictate" -> ∀(laws.dictateThenRetconIsDictate[A] _),
      "pure then materialize is right" -> ∀(laws.pureThenMaterializeIsRight[A] _),
      "pure then retcon is pure" -> ∀(laws.pureThenRetconIsPure[A] _)
    )
  }
}

object MonadChronicleTests {
  def apply[F[_], E](implicit instance: MonadChronicle[F, E]): MonadChronicleTests[F, E] = {
    new MonadChronicleTests[F, E] with Laws {
      override implicit val chronicleInstance: MonadChronicle[F, E] = instance
    }
  }
}
