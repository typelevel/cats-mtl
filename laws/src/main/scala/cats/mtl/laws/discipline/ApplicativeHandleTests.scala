package cats
package mtl
package laws
package discipline

import org.scalacheck.Prop.{forAll => ∀}
import org.scalacheck.{Arbitrary, Cogen}
import cats.kernel.laws.discipline.catsLawsIsEqToProp

trait ApplicativeHandleTests[F[_], E] extends FunctorRaiseTests[F, E] {
  implicit val handleInstance: ApplicativeHandle[F, E]

  override def laws: ApplicativeHandleLaws[F, E] = ApplicativeHandleLaws[F, E]

  def applicativeHandle[A: Arbitrary](implicit
                                        ArbFA: Arbitrary[F[A]],
                                        ArbEE: Arbitrary[E => E],
                                        ArbE: Arbitrary[E],
                                        CogenA: Cogen[A],
                                        CogenE: Cogen[E],
                                        EqFU: Eq[F[E]],
                                        EqFA: Eq[F[A]],
                                        EqFUnit: Eq[F[Unit]],
                                        EqEitherA: Eq[F[Either[E, A]]],
                                        EqEitherUnit: Eq[F[Either[E, Unit]]]
                                       ): RuleSet = {
    new DefaultRuleSet(
      name = "applicativeHandle",
      parent = Some(functorRaise[A]),
      "raise and handleWith is function application" -> ∀(laws.raiseAndHandleWithIsFunctionApplication[A]  _),
      "raise and handle is pure and function application" -> ∀(laws.raiseAndHandleIsPure[A] _),
      "pure and handleWith is pure" -> ∀(laws.handleWithPureIsPure[A] _),
      "pure and handle is pure" -> ∀(laws.handlePureIsPure[A] _),
      "pure and attempt is pure Right" -> ∀(laws.pureAttemptIsPureRight[A] _),
      "raise and attempt is pure Left" -> ∀(laws.raiseAttemptIsPureLeft _),
      "catch non fatal default" -> ∀(laws.catchNonFatalDefault[A] _)
    )
  }

}

object ApplicativeHandleTests {
  def apply[F[_], E](implicit instance0: ApplicativeHandle[F, E]): ApplicativeHandleTests[F, E] = {
    new ApplicativeHandleTests[F, E] {
      override lazy val handleInstance: ApplicativeHandle[F, E] = instance0
      override lazy val raiseInstance: FunctorRaise[F, E] = instance0
    }
  }
}
