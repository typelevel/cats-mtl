---
layout: docs
title:  "ApplicativeHandle"
section: "mtlclasses"
source: "core/src/main/scala/cats/mtl/ApplicativeHandle.scala"
scaladoc: "#cats.mtl.ApplicativeHandle"
---

## ApplicativeHandle

`ApplicativeHandle[F, E]` extends `FunctorRaise` with the ability to also handle raised errors.
It adds the `handleWith` function, which can be used to recover from errors and therefore allow continuing computations.


The `handleWith` function has the following signature:

```scala
def handleWith[A](fa: F[A])(f: E => F[A]): F[A]
```

This function is fully equivalent to `handleErrorWith` from `cats.ApplicativeError` and in general `ApplicativeHandle` is fully equivalent to `cats.ApplicativeError`,
 but is not a subtype of `Applicative` and therefore doesn't cause any ambiguitites.

Let's look at an example of how to use this function:

```tut:book
import cats._
import cats.data._
import cats.implicits._
import cats.mtl._
import cats.mtl.implicits._


// The function which might raise an error
def parseNumber[F[_]: Applicative](in: String)(implicit F: FunctorRaise[F, String]): F[Int] = {
  if (in.matches("-?[0-9]+")) in.toInt.pure[F]
  else F.raise(show"'$in' could not be parsed as a number")
}

def notRecovered[F[_]: Applicative](implicit F: FunctorRaise[F, String]): F[Boolean] = {
  parseNumber[F]("foo")
    .map(n => if (n > 5) true else false)
}

def recovered[F[_]: Applicative](implicit F: ApplicativeHandle[F, String]): F[Boolean] = {
  parseNumber[F]("foo")
    .handle[String](_ => 0) // Recover from error with fallback value
    .map(n => if (n > 5) true else false)
}

val err = notRecovered[Either[String, ?]]
val result = recovered[Either[String, ?]]
```
