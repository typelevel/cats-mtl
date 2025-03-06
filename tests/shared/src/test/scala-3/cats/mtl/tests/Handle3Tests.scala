package cats
package mtl
package tests

import cats.data.EitherT
import cats.mtl.syntax.all.*
import cats.syntax.all.*

class Handle3Tests extends BaseSuite:
  type F[A] = EitherT[Eval, Throwable, A]

  test("submerge custom errors (scala 3)"):
    enum Error:
      case First, Second, Third

    val test =
      Handle.allow[Error]:
        (Error.Second.raise.as("nope"): F[String])
      .rescue:
        case Error.First => "0".pure[F]
        case Error.Second => "1".pure[F]
        case Error.Third => "2".pure[F]

    assert(test.value.value.toOption == Some("1"))

  test("submerge two independent errors (scala 3)"):
    enum Error1:
      case First, Second, Third
    enum Error2:
      case Fourth
    val test =
      Handle.allow[Error1]:
        Handle.allow[Error2]:
          Error1.Third.raise[F, String].as("nope")
        .rescue:
          case e => e.toString.pure[F]
      .rescue:
        case Error1.First => "first1".pure[F]
        case Error1.Second => "second1".pure[F]
        case Error1.Third => "third1".pure[F]
    assert(test.value.value.toOption == Some("third1"))
