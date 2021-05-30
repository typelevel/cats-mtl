---
layout: docs
title:  "Handle"
section: "mtlclasses"
source: "core/src/main/scala/cats/mtl/Handle.scala"
scaladoc: "#cats.mtl.Handle"
---

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
import cats.implicits._
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
