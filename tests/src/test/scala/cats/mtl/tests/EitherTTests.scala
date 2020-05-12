package cats
package mtl
package tests

import cats._
import cats.arrow.FunctionK
import cats.data._
import cats.instances.all._
import cats.laws.discipline.SerializableTests
import cats.laws.discipline.arbitrary._
import cats.mtl.laws.discipline._
import org.scalacheck._

class EitherTTests extends BaseSuite {

  {

    checkAll("Either[String, *]", ApplicativeHandleTests[Either[String, *], String].applicativeHandle[Int])
    checkAll("EitherT[Option, String, *]",
      ApplicativeHandleTests[EitherT[Option, String, *], String].applicativeHandle[Int])

    checkAll("WriterT[Either[String, *], Int, *]",
      ApplicativeHandleTests[WriterT[Either[String, *], Int, *], String].applicativeHandle[Int])

    checkAll("WriterT[EitherT[Option, [String, *], Int, *]",
      ApplicativeHandleTests[WriterT[EitherT[Option, String, *], Int, *], String].applicativeHandle[Int])

  }

}
