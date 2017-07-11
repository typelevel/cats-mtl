package cats
package mtl
package laws
package discipline

import org.scalacheck.Arbitrary
import org.typelevel.discipline.Laws

trait ApplicativeLayerFunctorTests[M[_], Inner[_]] extends ApplicativeLayerTests[M, Inner] with FunctorLayerFunctorTests[M, Inner] {
  implicit val applicativeLayerFunctorInstance: ApplicativeLayerFunctor[M, Inner]
  override def laws: ApplicativeLayerFunctorLaws[M, Inner] = ApplicativeLayerFunctorLaws[M, Inner]
  def applicativeLayerFunctor[A: Arbitrary, B](implicit
                                    ArbMA: Arbitrary[M[A]],
                                    ArbFA: Arbitrary[Inner[A]],
                                    ArbFAB: Arbitrary[Inner[A => B]],
                                    ArbFun: Arbitrary[Inner ~> Inner],
                                    EqMA: Eq[M[A]],
                                    EqMB: Eq[M[B]]
                                   ): RuleSet = {
    new RuleSet {
      val name = "applicativeLayerFunctor"
      val parents = Seq(applicativeLayer[A, B], functorLayerFunctor[A])
      val props = Seq.empty
      def bases: Seq[(String, Laws#RuleSet)] = Seq.empty
    }
  }

}

object ApplicativeLayerFunctorTests {
  def apply[M[_], Inner[_]](implicit instance0: ApplicativeLayerFunctor[M, Inner]): ApplicativeLayerFunctorTests[M, Inner] = {
    new ApplicativeLayerFunctorTests[M, Inner] with Laws {
      override val applicativeLayerFunctorInstance: ApplicativeLayerFunctor[M, Inner] = instance0
      override val functorLayerFunctorInstance: FunctorLayerFunctor[M, Inner] = instance0
      override val applicativeLayerInstance: ApplicativeLayer[M, Inner] = instance0
      override val functorLayer: FunctorLayer[M, Inner] = instance0
    }
  }
}
