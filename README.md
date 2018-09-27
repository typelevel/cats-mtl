<img src="docs/src/main/resources/microsite/img/cats-mtl-logo.png" width="200px" height="231px" align="right">


## cats-mtl

[![Build Status](https://api.travis-ci.org/typelevel/cats-mtl.svg)](https://travis-ci.org/typelevel/cats-mtl)
[![codecov.io](http://codecov.io/github/typelevel/cats-mtl/coverage.svg?branch=master)](http://codecov.io/github/typelevel/cats-mtl?branch=master) [![Join the chat at https://gitter.im/cats-mtl/Lobby](https://badges.gitter.im/cats-mtl/Lobby.svg)](https://gitter.im/cats-mtl/Lobby?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

Transformer typeclasses for cats.

Provides transformer typeclasses for cats' Monads, Applicatives and Functors.

As well, there are some abstractions thrown in that allow you
to generically lift MTL typeclasses through transformers.

You can have multiple cats-mtl transformer typeclasses in scope at once
without implicit ambiguity, unlike in pre-1.0.0 cats or Scalaz 7.

## Usage

```scala
libraryDependencies += "org.typelevel" %% "cats-mtl-core" % "0.3.0"
```

If your project uses Scala.js, replace the double-`%` with a triple.  Note that **cats-mtl** has an upstream dependency on **cats-core** version 1.x.

Cross-builds are available for Scala 2.12 and 2.11, Scala.js major version 0.6.x.

### Laws

The **cats-mtl-laws** artifact provides [Discipline-style](https://github.com/typelevel/discipline) laws for all of the type classes defined in cats-mtl. It is relatively easy to use these laws to test your own implementations of these typeclasses. Take a look [here](https://github.com/typelevel/cats-mtl/tree/master/laws/shared/src/main/scala/cats/mtl/laws) for more.

```sbt
libraryDependencies += "org.typelevel" %% "cats-mtl-laws" % "0.3.0" % Test
```

These laws are compatible with both Specs2 and ScalaTest.

## Documentation

Links:

1. Website: [typelevel.org/cats-mtl/](https://typelevel.org/cats-mtl/)
2. ScalaDoc: [typelevel.org/cats-mtl/api/](https://typelevel.org/cats-mtl/api/)

Related Cats links (the core):

1. Website: [typelevel.org/cats/](https://typelevel.org/cats/)
2. ScalaDoc: [typelevel.org/cats/api/](https://typelevel.org/cats/api/)



#### MTL classes

cats-mtl provides the following "MTL classes":
 - `ApplicativeAsk`
 - `ApplicativeLocal`
 - `FunctorRaise`
 - `ApplicativeHandle`
 - `FunctorTell`
 - `FunctorListen`
 - `MonadState`
 - `MonadChroncicle`

All of these are typeclasses built on top of other "base" typeclasses to provide extra laws.
Because they are commonly combined, the base typeclasses are ambiguous in implicit scope using
the typical cats subclass encoding via subtyping. Thus in cats-mtl, the ambiguity is avoided by using
an implicit scope with a different priority to house each conversion `Subclass => Base`.

Compared to cats and Haskell's mtl library, cats-mtl's typeclasses have the smallest superclass dependencies
practically possible and in some cases smaller sets of operations so that they can be lifted over more transformers.

### Community

People are expected to follow the
[Typelevel Code of Conduct](http://typelevel.org/conduct.html) when
discussing cats-mtl on the Github page, Gitter channel, or other
venues.

We hope that our community will be respectful, helpful, and kind. If
you find yourself embroiled in a situation that becomes heated, or
that fails to live up to our expectations, you should disengage and
contact one of the [project maintainers](#maintainers) in private. We
hope to avoid letting minor aggressions and misunderstandings escalate
into larger problems.

