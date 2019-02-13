---
layout: docs
title:  "FunctorTell"
section: "mtlclasses"
source: "core/src/main/scala/cats/mtl/FunctorTell.scala"
scaladoc: "#cats.mtl.FunctorTell"
---

## FunctorTell

Data types that have instances of `FunctorTell[F, L]` are able to accumulate values of type `L` in the `F[_]` context.
This is done by adding values of `L` to the accumulated log by using the `tell` function:

```scala
trait FunctorTell[F[_], L] {
  def tell(l: L): F[Unit]
}
```

This log is write-only and the only way to modify it is to add more `L` values.
Usually most instances require a `Monoid[L]` in order to properly accumulate values of `L`.

Let's have a look at an example how you could `FunctorTell` to log service calls.
First we'll add the actual service call with its parameters and result types:


```tut:book
import cats._
import cats.data._
import cats.implicits._
import cats.mtl.FunctorTell
import cats.mtl.implicits._

case class ServiceParams(option1: String, option2: Int)

case class ServiceResult(userId: Int, companies: List[String]) 

// a fake call to some external service, replace with real implementation
def serviceCall[F[_]: Monad](params: ServiceParams): F[ServiceResult] = 
  ServiceResult(0, List("Raven Enterprises")).pure[F]
```

Now let's write an extra function that log what kind of parameters were passed as well as what the service returned.
For the log, we'll be using a `Chain[String]`, as it supports very efficient concatenation:

```tut:book

def serviceCallWithLog[F[_]: Monad](params: ServiceParams)(implicit F: FunctorTell[F, Chain[String]]): F[ServiceResult] =
  for {
    _ <- F.tell(Chain.one(show"Call to service with ${params.option1} and ${params.option2}"))
    result <- serviceCall[F](params)
    _ <- F.tell(Chain.one(show"Service returned: userId: ${result.userId}; companies: ${result.companies}"))
  } yield result
```

Now let's materialize this to see if it worked.
Instances for `FunctorTell` include `Writer` and `WriterT`, but also `Tuple2`:

  
```tut:book

val (log, result): (Chain[String], ServiceResult) =
  serviceCallWithLog[Writer[Chain[String], ?]](ServiceParams("business", 42)).run
```

And voila, it works as expected.
We were using just a standard `Writer` for this example,
  but remember that any monad transformer stack with `WriterT` in it somewhere will have an instance of `FunctorTell`.
