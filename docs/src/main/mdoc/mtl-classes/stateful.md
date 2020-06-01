---
layout: docs
title:  "Stateful"
section: "mtlclasses"
source: "core/src/main/scala/cats/mtl/Stateful.scala"
scaladoc: "#cats.mtl.Stateful"
---

## Stateful

`Stateful[F, S]` describes the capability to read and write state values of type `S` inside the `F[_]` context.
This materializes in two functions `get: F[S]` and `set: S => F[Unit]` that are needed for `Stateful` instances:


```scala
trait Stateful[F[_], S] {
  def get: F[S]
  def set(s: S): F[Unit]
}
```

Simply spoken `Stateful` gives us the ability to thread state through computations in `F[_]`.
It provides access to controlled mutable state, which can be very useful, but must also be used with care.

There are dozens of use cases where it might make sense to use `Stateful`,
 one of them is to use it to maintain a cache when repeatedly accessing external services.
If we've already accessed a service with a given identifier, we can then access the cache instead, of course we also want the ability to invalidate the cache to receive fresh values as well.

Let's say we have the following function to access a service:

```scala mdoc
import cats._
import cats.data._
import cats.implicits._
import cats.mtl.Stateful

case class ServiceResult(id: Int, companies: List[String])

def serviceCall[F[_]: Monad](id: String): F[ServiceResult] = {
  // a fake call to some external service, impure, so don't do this at home!
  println(show"Called service with $id")
  ServiceResult(0, List("Raven Enterprises")).pure[F]
}
```

Now, we want a new function that looks inside of a cache for the desired value and if it's not there, access the service and put the result inside the cache.
To do so, we'll make use of `Stateful` with a simple `Map[String, ServiceResult]` as our cache:

```scala mdoc
type Cache = Map[String, ServiceResult]

def cachedServiceCall[F[_]: Monad](id: String)(implicit F: Stateful[F, Cache]): F[ServiceResult] = for {
  cache <- F.get
  result <- cache.get(id) match {
              case Some(result) => result.pure[F]
              case None => serviceCall[F](id)
            }
} yield result
```

So far, so good, we're successfully reading from the cache if it has the value we need, but it also doesn't actually write anything into the cache when we call our service.
Let's try again to write to the cache after making the service call.

```scala mdoc

def serviceCallAndWriteToCache[F[_]: Monad](id: String)(implicit F: Stateful[F, Cache]): F[ServiceResult] = for {
  result <- serviceCall[F](id)
  cache <- F.get
  _ <- F.set(cache.updated(id, result))
} yield result
```

Lastly, we want to be able to invalidate our cache. This is fairly simple, as all we need to do is to use `set` with an empty cache:

```scala mdoc
def invalidate[F[_]](implicit F: Stateful[F, Cache]): F[Unit] = F.set(Map.empty)
```

Now that we have our building blocks, we can now build a program that makes a few requests then invalidates the cache and makes another request:


```scala mdoc
def program[F[_]: Monad](implicit F: Stateful[F, Cache]): F[ServiceResult] = for {
  result1 <- cachedServiceCall[F]("ab94d2")
  result2 <- cachedServiceCall[F]("ab94d2") // This should use the cached value
  _ <- invalidate[F]
  freshResult <- cachedServiceCall[F]("ab94d2") // This should access the service again
} yield freshResult
```

And we're done, now, to be able to run this, we need to materialize our `program` into an actual value.
As the name suggests, `State` and `StateT` both allow us to use `Stateful`:

```scala mdoc
val initialCache: Cache = Map.empty

val (result, cache) = program[State[Cache, ?]].run(initialCache).value
```

As per usual, Cats-mtl provides `Stateful` instances for all monad transformer stacks where `StateT` appears.
