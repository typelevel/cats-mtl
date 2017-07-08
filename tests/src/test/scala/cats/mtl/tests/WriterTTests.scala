package cats
package mtl
package tests

import cats._
import cats.arrow.FunctionK
import cats.data._
import cats.instances.all._
import cats.laws.discipline.SerializableTests
import cats.mtl.instances.all._
import cats.laws.discipline.arbitrary._
import cats.mtl.laws.discipline._
import org.scalacheck._

class WriterTTests extends BaseSuite {
  checkAll("WriterT[Option, String, String]",
    FunctorListenTests[WriterTC[Option, String]#l, String].functorListen[String, String])
  checkAll("FunctorListen[WriterT[Option, String, ?]]",
    SerializableTests.serializable(FunctorListen[WriterTC[Option, String]#l, String]))

  implicit val arbFunctionK: Arbitrary[Option ~> Option] =
    Arbitrary(Gen.oneOf(new (Option ~> Option) {
      def apply[A](fa: Option[A]): Option[A] = None
    }, FunctionK.id[Option]))

  {
    checkAll("WriterT[Option, String, ?]",
      ApplicativeLayerFunctorTests[WriterTC[Option, String]#l, Option].applicativeLayerFunctor[Boolean, Boolean])
    checkAll("ApplicativeLayerFunctor[WriterT[Option, String, ?], Option]",
      SerializableTests.serializable(ApplicativeLayerFunctor[WriterTC[Option, String]#l, Option]))
  }

  {
    checkAll("WriterT[Option, String, ?]",
      FunctorLayerFunctorTests[WriterTC[Option, String]#l, Option].functorLayerFunctor[Boolean])
    checkAll("FunctorLayerFunctor[WriterT[Option, String, ?], Option]",
      SerializableTests.serializable(FunctorLayerFunctor[WriterTC[Option, String]#l, Option]))
  }
}
