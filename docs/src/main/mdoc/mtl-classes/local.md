---
layout: docs
title:  "Local"
section: "mtlclasses"
source: "core/src/main/scala/cats/mtl/Local.scala"
scaladoc: "#cats.mtl.Local"
---

## Local

`Local[F, E]` extends `Ask` and allows to us express local modifications of the environment.
In practice, this means, that whenever we use `local` on a value of `ask`, we can locally modify the environment with a function `E => E`.
In this case, locally modifying means that the only place we can actually observe the modification is in the `F[A]` value passed to the `local` function.

The `local` function has the following signature: 

```scala
trait Local[F[_], E] extends Ask[F, E] {
  def local(f: E => E)(fa: F[A]): F[A]
}
```


Here's a quick example of how it works:

```scala mdoc
import cats._
import cats.data._
import cats.implicits._
import cats.mtl._

def calculateContentLength[F[_]: Applicative](implicit F: Ask[F, String]): F[Int] =
  F.ask.map(_.length)
  
def calculateModifiedContentLength[F[_]: Applicative](implicit F: Local[F, String]): F[Int] =
  F.local(calculateContentLength[F])("Prefix " + _)

val result = calculateModifiedContentLength[Reader[String, ?]].run("Hello")

```

We can see that the content length returned is 12, because the string `"Prefix "` is prepended to the environment value.
To see that `local` only applies to the passed parameter we can run both `calculateContentLength` and
 `calculateModifiedContentLength` in one for-expression:

```scala mdoc
def both[F[_]: Monad](implicit F: Local[F, String]): F[(Int, Int)] = for {
  length <- calculateContentLength[F]
  modifiedLength <- calculateModifiedContentLength[F]
} yield (length, modifiedLength)

val res = both[Reader[String, ?]].run("Hello")
```
