---
layout: docs
title:  "FunctorRaise"
section: "mtlclasses"
source: "core/src/main/scala/cats/mtl/FunctorRaise.scala"
scaladoc: "#cats.mtl.FunctorRaise"
---

## FunctorRaise

`FunctorRaise[F, E]` expresses the ability to raise errors of type `E` in a functorial `F[_]` context.
This means that a value of type `F[A]` may contain no `A` values but instead an `E` error value,
and further `map` calls will not have any effect due to there being no value to execute the passed function on.

In that sense it is a less powerful version of `ApplicativeError` found in cats-core.
In general, however, `raiseError` from `ApplicativeError` and `raise` from `FunctorRaise` should be equivalent.

The `raise` function has the following signature:

```scala
trait FunctorRaise[F[_], E] {
  def raise[A](e: E): F[A]
}
```

Here's a quick example of how you might use it:

```scala mdoc
import cats._
import cats.implicits._
import cats.mtl._

def parseNumber[F[_]: Applicative](in: String)(implicit F: FunctorRaise[F, String]): F[Int] = {
  if (in.matches("-?[0-9]+")) in.toInt.pure[F]
  else F.raise(show"'$in' could not be parsed as a number")
}

val valid = parseNumber[Either[String, ?]]("123")
val invalid = parseNumber[Either[String, ?]]("123abc")
```
