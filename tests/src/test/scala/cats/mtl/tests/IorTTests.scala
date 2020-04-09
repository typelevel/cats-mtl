package cats
package mtl
package tests

import cats.arrow.FunctionK
import cats.data.Ior
import cats.instances.all._
import cats.mtl.instances.chronicle._
import cats.laws.discipline.SerializableTests
import cats.laws.discipline.arbitrary._
import cats.mtl.laws.discipline._
import org.scalacheck._

class IorTTests extends BaseSuite {
  implicit val arbFunctionK: Arbitrary[Option ~> Option] =
    Arbitrary(Gen.oneOf(new (Option ~> Option) {
      def apply[A](fa: Option[A]): Option[A] = None
    }, FunctionK.id[Option]))

  implicit def arbFunctionKIor[A]: Arbitrary[Ior[A, *] ~> Ior[A, *]] =
    Arbitrary(Gen.oneOf(new (Ior[A, *] ~> Ior[A, *]) {
      override def apply[B](fa: Ior[A, B]): Ior[A, B] = fa
    }, FunctionK.id[Ior[A, *]]))

  {
    import cats.mtl.instances.writert._

    checkAll(
      "IorT[Option, String, *]",
      MonadChronicleTests[IorTC[Option, String]#l, String]
        .monadChronicle[String]
    )

    checkAll(
      "Ior[String, String]",
      MonadChronicleTests[IorC[String]#l, String]
        .monadChronicle[String]
    )

    checkAll(
      "WriterT[Ior[String, *], Int]",
      MonadChronicleTests[WriterTC[IorC[String]#l, Int]#l, String].monadChronicle[String]
    )

    checkAll(
      "WriterT[IorT[Option, String, *], Int]",
      MonadChronicleTests[WriterTC[IorTC[Option, String]#l, Int]#l, String].monadChronicle[String]
    )

  }
}
