---
layout: docs
title:  "ApplicativeLocal"
section: "mtlclasses"
source: "core/src/main/scala/cats/mtl/ApplicativeLocal.scala"
scaladoc: "#cats.mtl.ApplicativeLocal"
---

## ApplicativeLocal

`ApplicativeLocal[F, E]` extends `ApplicativeAsk` and allows us express local modifications of the environment.
In practical terms, this means, that if we have an instance of `ApplicativeAsk[F, E]`,
 we can now request a value of `F[E]` by using the `ask` method.

Simplified, it is defined like this:

```tut:book
trait ApplicativeAsk[F[_], E] {
  def ask: F[E]
}
```

This typeclass comes in handy whenever we want to be able to request a value from the environment,
 e.g. read from some external configuration.

### Instances

A trivial instance for `ApplicativeAsk` can be just a plain function:

```tut:book
def functionApplicativeAsk[E]: ApplicativeAsk[E => ?, E] = new ApplicativeAsk[E => ?, E] {
  def ask: E => E = identity
}
```

Other instances include `Reader` and `ReaderT`/`Kleisli`.
In fact the implementation for `ask` is just `ReaderT.ask`, so it's a perfect fit.

Cats-mtl is able to lift `ApplicativeAsk` instances through a monad transformer stack, so for example,
`StateT[Reader[E, ?], S, A]` and `EitherT[ReaderT[List, E, ?], Error, A]` will both have `ApplicativeAsk` instances whenever you import from  `cats.mtl.instances._`.

In general every monad transformer stack where `Reader` appears once, will have an instance of `ApplicativeAsk`.
