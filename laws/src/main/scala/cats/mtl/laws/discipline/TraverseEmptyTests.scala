package cats
package mtl
package laws
package discipline

import cats.data.Nested
import org.scalacheck.Prop.{forAll => ∀}
import org.scalacheck.Arbitrary
import cats.instances.option._
import cats.kernel.laws.discipline.catsLawsIsEqToProp

trait TraverseEmptyTests[F[_]] extends FunctorEmptyTests[F] {
  implicit val traverseEmptyInstance: TraverseEmpty[F]

  override val laws: TraverseEmptyLaws[F] = TraverseEmptyLaws[F]

  def traverseEmpty[A, B, C](implicit
                             ArbFA: Arbitrary[F[A]],
                             ArbFOA: Arbitrary[F[Option[A]]],
                             ArbFABoo: Arbitrary[PartialFunction[A, B]],
                             ArbAOB: Arbitrary[A => Option[B]],
                             ArbAOOB: Arbitrary[A => Option[Option[B]]],
                             ArbBOC: Arbitrary[B => Option[C]],
                             ArbBOOC: Arbitrary[B => Option[Option[C]]],
                             ArbAB: Arbitrary[A => B],
                             ArbABoo: Arbitrary[A => Boolean],
                             ArbAOBoo: Arbitrary[A => Option[Boolean]],
                             EqFA: Eq[F[A]],
                             EqFB: Eq[F[B]],
                             EqFC: Eq[F[C]],
                             EqGFA: Eq[Option[F[A]]],
                             EqMNFC: Eq[Nested[Option, Option, F[C]]]
                            ): RuleSet = {
    new DefaultRuleSet(
      name = "traverseEmpty",
      parent = Some(functorEmpty[A, B, C]),
      "traverseFilter identity" -> ∀(laws.traverseFilterIdentity[Option, A] _),
      "traverseFilter nested composition" -> ∀(laws.traverseFilterComposition[A, B, C, Option, Option] _),
      "filterA consistent with traverseFilter" -> ∀(laws.filterAConsistentWithTraverseFilter[Option, A] _)
    )
  }
}

object TraverseEmptyTests {
  def apply[F[_]](implicit traverseEmptyInstance0: TraverseEmpty[F]): TraverseEmptyTests[F] = {
    new TraverseEmptyTests[F] {
      override lazy val traverseEmptyInstance: TraverseEmpty[F] = traverseEmptyInstance0
      override lazy val functorEmptyInstance: FunctorEmpty[F] = traverseEmptyInstance0
    }
  }
}
