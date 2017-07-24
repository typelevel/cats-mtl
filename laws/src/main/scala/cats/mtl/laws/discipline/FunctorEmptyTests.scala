package cats
package mtl
package laws
package discipline

import org.scalacheck.Prop.{forAll => ∀}
import org.scalacheck.Arbitrary
import org.typelevel.discipline.Laws

trait FunctorEmptyTests[F[_]] extends Laws {
  implicit val functorEmptyInstance: FunctorEmpty[F]
  implicit val laws: FunctorEmptyLaws[F] = FunctorEmptyLaws[F]

  def functorEmpty[A, B, C](implicit
                            ArbFA: Arbitrary[F[A]],
                            ArbAOB: Arbitrary[A => Option[B]],
                            ArbBOC: Arbitrary[B => Option[C]],
                            ArbAB: Arbitrary[A => B],
                            EqFB: Eq[F[B]],
                            EqFC: Eq[F[C]]
                           ): RuleSet = {
    new DefaultRuleSet(
      name = "functorEmpty",
      parent = None,
      "mapFilter composition" -> ∀(laws.mapFilterComposition[A, B, C] _),
      "mapFilter map consistency" -> ∀(laws.mapFilterMapConsistency[A, B] _)
    )
  }
}

object FunctorEmptyTests {
  def apply[F[_]](implicit functorEmptyInstance0: FunctorEmpty[F]): FunctorEmptyTests[F] = {
    new FunctorEmptyTests[F] {
      override lazy val functorEmptyInstance: FunctorEmpty[F] = functorEmptyInstance0
    }
  }
}
