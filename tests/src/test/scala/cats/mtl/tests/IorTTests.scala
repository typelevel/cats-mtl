package cats.mtl.tests

import cats.arrow.FunctionK
import cats.data.Ior
import cats.instances.all._
import cats.laws.discipline.SerializableTests
import cats.laws.discipline.arbitrary._
import cats.mtl.laws.discipline._
import cats.mtl.{FunctorLayerFunctor, IorC, IorTC, MonadLayerControl}
import cats.~>
import org.scalacheck._

/**
  * Created by Yuval.Itzchakov on 16/07/2018.
  */
class IorTTests extends BaseSuite {
  implicit val arbFunctionK: Arbitrary[Option ~> Option] =
    Arbitrary(Gen.oneOf(new (Option ~> Option) {
      def apply[A](fa: Option[A]): Option[A] = None
    }, FunctionK.id[Option]))

  implicit def arbFunctionKIor[A]: Arbitrary[Ior[A, ?] ~> Ior[A, ?]] =
    Arbitrary(Gen.oneOf(new (Ior[A, ?] ~> Ior[A, ?]) {
      override def apply[B](fa: Ior[A, B]): Ior[A, B] = fa
    }, FunctionK.id[Ior[A, ?]]))

  {
    implicit val monadLayerControl
      : MonadLayerControl.Aux[IorTC[Option, String]#l, Option, IorC[String]#l] =
      cats.mtl.instances.iort.iorMonadLayerFunctor[Option, String]

    checkAll("IorT[Option, String, ?]",
             MonadLayerControlTests[IorTC[Option, String]#l, Option, IorC[String]#l]
               .monadLayerControl[Boolean, Boolean])
    checkAll("MonadLayerControl[IorT[Option, String, ?], Option]",
             SerializableTests.serializable(monadLayerControl))
  }

  {
    implicit val functorLayerFunctor: FunctorLayerFunctor[IorTC[Option, String]#l, Option] =
      cats.mtl.instances.iort.iorFunctorLayerFunctor[Option, String]
    checkAll("IorT[Option, ?]",
             FunctorLayerFunctorTests[IorTC[Option, String]#l, Option]
               .functorLayerFunctor[Boolean])
    checkAll("FunctorLayerFunctor[IorT[Option, String, ?], Option]",
             SerializableTests.serializable(functorLayerFunctor))
  }
}