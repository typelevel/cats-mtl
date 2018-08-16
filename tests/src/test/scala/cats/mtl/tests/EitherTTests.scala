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
import cats.mtl.lifting.{FunctorLayerFunctor, MonadLayerControl}
import org.scalacheck._

class EitherTTests extends BaseSuite {
  {
    implicit val monadLayerControl: MonadLayerControl.Aux[EitherTC[Option, String]#l, Option, EitherC[String]#l] =
      cats.mtl.instances.eithert.eitherMonadLayerControl[Option, String]
    checkAll("EitherT[Option, String, ?]",
      MonadLayerControlTests[EitherTC[Option, String]#l, Option, EitherC[String]#l]
        .monadLayerControl[Boolean, Boolean])
    checkAll("MonadLayerControl[EitherT[Option, String, ?], Option]",
      SerializableTests.serializable(monadLayerControl))
  }

  {
    implicit val functorLayerFunctor: FunctorLayerFunctor[EitherTC[Option, String]#l, Option] =
      cats.mtl.instances.eithert.eitherFunctorLayerFunctor[Option, String]
    checkAll("EitherT[Option, String, ?]",
      FunctorLayerFunctorTests[EitherTC[Option, String]#l, Option]
        .functorLayerFunctor[Boolean])
    checkAll("FunctorLayerFunctor[EitherT[Option, String, ?], Option]",
      SerializableTests.serializable(functorLayerFunctor))
  }

  {
    import cats.mtl.instances.handle._
    import cats.mtl.instances.eithert._
    import cats.mtl.instances.writert._

    checkAll("Either[String, ?]", ApplicativeHandleTests[Either[String, ?], String].applicativeHandle[Int])
    checkAll("EitherT[Option, String, ?]",
      ApplicativeHandleTests[EitherT[Option, String, ?], String].applicativeHandle[Int])

    checkAll("WriterT[Either[String, ?], Int, ?]",
      ApplicativeHandleTests[WriterT[Either[String, ?], Int, ?], String].applicativeHandle[Int])

    checkAll("WriterT[EitherT[Option, [String, ?], Int, ?]",
      ApplicativeHandleTests[WriterT[EitherT[Option, String, ?], Int, ?], String].applicativeHandle[Int])

  }

}
