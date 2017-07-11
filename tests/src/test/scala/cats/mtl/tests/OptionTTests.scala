package cats
package mtl
package tests

import cats._
import cats.arrow.FunctionK
import cats.data._
import cats.instances.all._
import cats.laws.discipline.SerializableTests
import cats.mtl.instances.listen._
import cats.laws.discipline.arbitrary._
import cats.mtl.laws.discipline._
import org.scalacheck._

class OptionTTests extends BaseSuite {
  implicit val arbFunctionK: Arbitrary[Option ~> Option] =
    Arbitrary(Gen.oneOf(new (Option ~> Option) {
      def apply[A](fa: Option[A]): Option[A] = None
    }, FunctionK.id[Option]))

  {
    implicit val monadLayerFunctor: MonadLayerFunctor[OptionTC[Option]#l, Option] =
      cats.mtl.instances.optiont.optionMonadLayerControl[Option]
    checkAll("OptionT[Option, String, ?]",
      MonadLayerFunctorTests[OptionTC[Option]#l, Option].monadLayerFunctor[Boolean, Boolean])
    checkAll("MonadLayerFunctor[OptionT[Option, String, ?], Option]",
      SerializableTests.serializable(monadLayerFunctor))
  }

  {
    implicit val applicativeLayerFunctor: ApplicativeLayerFunctor[OptionTC[Option]#l, Option] =
      cats.mtl.instances.optiont.optionApplicativeLayerFunctor[Option]
    checkAll("OptionT[Option, String, ?]",
      ApplicativeLayerFunctorTests[OptionTC[Option]#l, Option].applicativeLayerFunctor[Boolean, Boolean])
    checkAll("ApplicativeLayerFunctor[OptionT[Option, String, ?], Option]",
      SerializableTests.serializable(applicativeLayerFunctor))
  }

  {
    implicit val functorLayerFunctor: FunctorLayerFunctor[OptionTC[Option]#l, Option] =
      cats.mtl.instances.optiont.optionFunctorLayerFunctor[Option]
    checkAll("OptionT[Option, ?]",
      FunctorLayerFunctorTests[OptionTC[Option]#l, Option].functorLayerFunctor[Boolean])
    checkAll("FunctorLayerFunctor[OptionT[Option, ?], Option]",
      SerializableTests.serializable(functorLayerFunctor))
  }
}
