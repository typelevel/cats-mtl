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
    import cats.mtl.instances.handle._
    import cats.mtl.instances.optiont._
    import cats.mtl.instances.writert._

    checkAll("Option", ApplicativeHandleTests[Option, Unit].applicativeHandle[Int])
    checkAll("OptionT[Either[String, *], *]",
      ApplicativeHandleTests[OptionT[Either[String, *], *], Unit].applicativeHandle[Int])

    checkAll("WriterT[Option, Int, *]",
      ApplicativeHandleTests[WriterT[Option, Int, *], Unit].applicativeHandle[Int])

    checkAll("WriterT[OptionT[Either[String, *], *], Int, *]",
      ApplicativeHandleTests[WriterT[OptionT[Either[String, *], *], Int, *], Unit].applicativeHandle[Int])

  }
}
