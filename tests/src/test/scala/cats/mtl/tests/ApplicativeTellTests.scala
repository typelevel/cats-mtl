package cats
package mtl
package tests

import cats.mtl.laws.ApplicativeTellLaws
import org.scalacheck.{Arbitrary, Cogen, Prop}
import Prop._
import org.typelevel.discipline.Laws

abstract class ApplicativeTellTests[F[_], L] extends Laws {
  def laws: ApplicativeTellLaws[F, L]

  def applicativeTell[A: Arbitrary](implicit
    ArbFA: Arbitrary[F[A]],
    ArbL: Arbitrary[L],
    CogenA: Cogen[A],
    EqFU: Eq[F[Unit]],
    EqFA: Eq[F[A]]
  ): RuleSet = {
    new DefaultRuleSet(
      name = "applicativeTell",
      parent = None,
      "tell forms semigroup action" -> forAll(laws.tellTwiceIsTellCombined _),
      "tell forms monoid action" -> (laws.tellEmptyIsPureUnit: Prop),
      "tuple is (flip writer)" -> forAll(laws.tupleIsWriterFlipped[A] _),
      "applicative interchange" -> forAll(laws.writerIsTellAndMap[A] _)
    )
  }

}

object ApplicativeTellTests {
  def apply[F[_], L](implicit tell: ApplicativeTell[F, L]): ApplicativeTellTests[F, L] =
    new ApplicativeTellTests[F, L] {
      def laws: ApplicativeTellLaws[F, L] = ApplicativeTellLaws[F, L]
    }
}

