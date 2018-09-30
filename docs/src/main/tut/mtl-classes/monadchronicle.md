---
layout: docs
title:  "MonadChronicle"
section: "mtlclasses"
source: "core/src/main/scala/cats/mtl/MonadChronicle.scala"
scaladoc: "#cats.mtl.MonadChronicle"
---

## MonadChronicle

`MonadChronicle[F, E]` allows us to both accumulate values and also short-circuit computations with a final output.
In that sense it can be seen as a hybrid of `FunctorTell` and `ApplicativeHandle`/`MonadError`.

`MonadChronicle` is defined by 3 distinct operators, to understand it better, let's look at a simplified definition:

```scala
trait MonadChronicle[F[_], E] {
  def dictate(c: E): F[Unit]

  def confess[A](c: E): F[A]

  def materialize[A](fa: F[A]): F[E Ior A]
}
```

If you're already familiar with `FunctorTell`, `dictate` should look familiar.
In fact, it has the same signature as `FunctorTell#tell` and achieves roughly the same thing,
 appending a value of `E` to some internal log.

Next, we have `confess`, which might seems familar if you know `FunctorRaise`/`ApplicativeError`'s `raise`/raiseError` method.
It also does the same thing here, which is raising an error that will short-circuit the computation.

This means, that internally, instances of `MonadChronicle` need to differentiate between values of type `E`.
`dictate` adds a value of `E` to a log, but allows the computation to continue.
Values coming from `confess` on the other hand, stop all computations afterwards and short-circuit,
 since calls to `flatMap` will no longer have any effect.

The only way to recover from the short-circuiting behaviour of `confess` is to use `MonadChronicle`'s third method `materialize`.
`materialize` takes an `F[A]` and returns an `F[E Ior A]`.
`Ior` represents an inclusive-or relationship, meaning an `E Ior A` can either be an `E`, an `A` or a tuple of both `(E, A)`.
You can read more about `Ior` in the [cats documentation](https://typelevel.org/cats/datatypes/ior.html).

These three cases perfectly represent in which state the `F[A]` was in.
If the `F[A]` passed to `materialize` was short-circuited by `confess`, then the result will be only an `E`.
If `F[A]` at some point involved a call to `dictate`, meaning it had accumulated a log of `E`, then the result will be a pair of the log `E` and the resulting value `A`.
If neither `confess` or `dictate` were used to construct the `F[A]`, then `materialize` will only return the resulting value `A`.

To gain a better understanding, let's look at an example.
Some applications call for a notion of *non-fatal errors*, along with the more standard fatal errors that halt computations.
`MonadChronicle` is perfect for these kinds of use cases.
Let's say we want to create a sign up where users can register with username and password.
We can prohibit bad user data by using a computation halting `confess` and nudge them in the right direction by giving warnings when e.g. their password is fairly short:

```tut:book
import cats.Monad
import cats.data._
import cats.implicits._
import cats.mtl.MonadChronicle
import cats.mtl.implicits._

type Failures = NonEmptyChain[String]

case class Username(value: String)
case class Password(value: String)

case class User(name: Username, pw: Password)

def validateUsername[F[_]: Monad](u: String)(implicit F: MonadChronicle[F, Failures]): F[Username] = {
  if (u.isEmpty)
    F.confess(NonEmptyChain.one("Can't be empty"))
  else if (u.contains("."))
    F.dictate(NonEmptyChain.one("Dot in name is deprecated")).map(_ => Username(u))
  else
    Username(u).pure[F]
}

def validatePassword[F[_]: Monad](p: String)(implicit F: MonadChronicle[F, Failures]): F[Password] = {
  if (p.length < 8)
    F.confess(NonEmptyChain.one("Password too short"))
  else if (p.length < 10)
    F.dictate(NonEmptyChain.one("Password should be longer")).map(_ => Password(p))
  else
    Password(p).pure[F]
}

def validateUser[F[_]: Monad](name: String, password: String)(implicit F: MonadChronicle[F, Failures]): F[User] =
  (validateUsername[F](name), validatePassword[F](password)).mapN(User)

```

Now we can fully validate users and accumulate a log of warnings or fail entirely when an invalid name or password was encountered.
Pretty neat, next let's actually run this program to see if what we did was correct.
We can do that with `Ior`:

```tut:book
val luka = validateUser[Ior[Failures, ?]]("Luka", "secret")
val john = validateUser[Ior[Failures, ?]]("john.doe", "secret123")
val jane = validateUser[Ior[Failures, ?]]("jane", "reallysecurepassword")
```


Apart from `Ior` instances for `MonadChronicle` also include its `IorT` and any monad transformer stack where `Ior` or `IorT` appear.
