package cats
package mtl
package tests

import cats.data.EitherT
import cats.laws.discipline.arbitrary._
import cats.mtl.laws.discipline.HandleTests
import cats.mtl.syntax.all._
import cats.syntax.all._

import org.scalacheck.{Arbitrary, Cogen}, Arbitrary.arbitrary

class AdHocHandleTests extends BaseSuite {

  type F[A] = EitherT[Eval, Throwable, A]

  test("submerge custom errors") {
    sealed trait Error extends Product with Serializable

    object Error {
      case object First extends Error
      case object Second extends Error
      case object Third extends Error
    }

    val test =
      Handle.ensure[F, Error](implicit h => Error.Second.raise.as("nope")) recover {
        case Error.First => "0".pure[F]
        case Error.Second => "1".pure[F]
        case Error.Third => "2".pure[F]
      }

    assert(test.value.value.toOption == Some("1"))
  }

  test("submerge two independent errors") {
    sealed trait Error1 extends Product with Serializable

    object Error1 {
      case object First extends Error1
      case object Second extends Error1
      case object Third extends Error1
    }

    sealed trait Error2 extends Product with Serializable

    val test = Handle.ensure[F, Error1] { implicit h1 =>
      Handle.ensure[F, Error2] { implicit h2 =>
        val _ =
          h2 // it's helpful to test the raise syntax infers even when multiple handles are present
        Error1.Third.raise.as("nope")
      } recover { e => e.toString.pure[F] }
    } recover {
      case Error1.First => "first1".pure[F]
      case Error1.Second => "second1".pure[F]
      case Error1.Third => "third1".pure[F]
    }

    assert(test.value.value.toOption == Some("third1"))
  }

  {
    final case class Error(value: Int)

    object Error {
      implicit val arbError: Arbitrary[Error] =
        Arbitrary(arbitrary[Int].flatMap(Error(_)))

      implicit val cogenError: Cogen[Error] =
        Cogen((_: Error).value.toLong)

      implicit val eqError: Eq[Error] =
        Eq.by((_: Error).value)
    }

    implicit val eqThrowable: Eq[Throwable] =
      Eq.fromUniversalEquals[Throwable]

    val test = Handle.ensure[F, Error] { implicit h =>
      EitherT liftF {
        Eval later {
          checkAll("Handle.ensure[F, Error]", HandleTests[F, Error].handle[Int])
        }
      }
    } recover { case Error(_) => ().pure[F] }

    test.value.value
    ()
  }
}
