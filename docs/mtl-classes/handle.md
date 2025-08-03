## Handle

`Handle[F, E]` extends `Raise` with the ability to also handle raised errors.
It adds the `handleWith` function, which can be used to recover from errors and therefore allow continuing computations.


The `handleWith` function has the following signature:

```scala
def handleWith[A](fa: F[A])(f: E => F[A]): F[A]
```

This function is fully equivalent to `handleErrorWith` from `cats.ApplicativeError` and in general `Handle` is fully equivalent to `cats.ApplicativeError`,
 but is not a subtype of `Applicative` and therefore doesn't cause any ambiguitites.

Let's look at an example of how to use this function:

```scala mdoc
import cats._
import cats.syntax.all._
import cats.mtl._
import cats.mtl.implicits._

def parseNumber[F[_]: Applicative](in: String)(implicit F: Raise[F, String]): F[Int] = {
  // this function might raise an error
  if (in.matches("-?[0-9]+")) in.toInt.pure[F]
  else F.raise(show"'$in' could not be parsed as a number")
}

def notRecovered[F[_]: Applicative](implicit F: Raise[F, String]): F[Boolean] = {
  parseNumber[F]("foo")
    .map(n => if (n > 5) true else false)
}

def recovered[F[_]: Applicative](implicit F: Handle[F, String]): F[Boolean] = {
  parseNumber[F]("foo")
    .handle[String](_ => 0) // Recover from error with fallback value
    .map(n => if (n > 5) true else false)
}

val err = notRecovered[Either[String, *]]
val result = recovered[Either[String, *]]
```

### Error propagation with `allow` and `rescue`

In addition to the traditional `raise` and `handleWith` mechanism, `Handle` supports `allow` and `rescue` functions which provides a concise, intuitive and performant way to raise and handle domain-specific errors, with syntax inspired by `try/catch` blocks.

@:select(scala-version)

@:choice(scala-3)

```scala
import cats.*
import cats.syntax.all.*
import cats.mtl.*
import cats.mtl.Handle.*

type F[A] = Either[Throwable, A]

enum DomainError:
  case Failed
  case Derped

def foo(using h: Raise[F, DomainError]): F[String] = Either.right("foo")
def bar(using h: Handle[F, DomainError]): F[String] = h.raise(DomainError.Failed)

val submarine: F[String] =
  allow:
    foo *> bar
  .rescue:
    case DomainError.Failed => Either.right("Handled Failed")
    case DomainError.Derped => Either.right("Handled Derped")
// submarine: Either[Throwable, String] = Right(value = "Handled Failed")
```

@:choice(scala-2)

```scala mdoc
import cats._
import cats.syntax.all._
import cats.mtl._
import cats.mtl.Handle._

type F[A] = Either[Throwable, A]

sealed trait DomainError
object DomainError {
  case object Failed extends DomainError
  case object Derped extends DomainError
}

def foo(implicit h: Raise[F, DomainError]): F[String] = Either.right("foo")
def bar(implicit h: Handle[F, DomainError]): F[String] = h.raise(DomainError.Failed)

val submarine: F[String] =
  allowF[F, DomainError] { implicit h =>
    foo *> bar
  } rescue {
    case DomainError.Failed => Either.right("Handled Failed")
    case DomainError.Derped => Either.right("Handled Derped")
  }
```

@:@

Notice that `DomainError` is a regular ADT (algebraic data type) and does not extend `Exception`. With `allow` (`allowF` in Scala 2) and `rescue`, you can raise and handle errors of your own types—not just exceptions—using syntax that feels like familiar exception handling, but in a purely functional and type-safe way.

This pattern makes error handling more readable and expressive, and often removes the need for transformers like `EitherT` or `IorT` in regular code.
