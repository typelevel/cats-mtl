<img src="https://github.com/typelevel/cats-mtl/raw/master/docs/src/main/resources/microsite/img/cats-mtl-logo-microsite.png" width="200px" height="231px" align="right">


## cats-mtl

[![Build Status](https://api.travis-ci.org/typelevel/cats-mtl.svg)](https://travis-ci.org/typelevel/cats-mtl)
[![codecov.io](http://codecov.io/github/typelevel/cats-mtl/coverage.svg?branch=master)](http://codecov.io/github/typelevel/cats-mtl?branch=master) [![Join the chat at https://gitter.im/typelevel/cats-mtl](https://badges.gitter.im/typelevel/cats-mtl.svg)](https://gitter.im/typelevel/cats-mtl)

Transformer typeclasses for cats.

Provides transformer typeclasses for cats' Monads, Applicatives and Functors.

As well, there are some abstractions thrown in that allow you
to generically lift MTL typeclasses through transformers.

You can have multiple cats-mtl transformer typeclasses in scope at once
without implicit ambiguity, unlike in pre-1.0.0 cats or Scalaz 7.

## Usage

```scala
libraryDependencies += "org.typelevel" %% "cats-mtl-core" % "0.6.0"
```

If your project uses Scala.js, replace the double-`%` with a triple.  Note that **cats-mtl** has an upstream dependency on **cats-core** version 1.x.

Cross-builds are available for Scala 2.11–2.13, Scala.js major version 0.6.x.

If you're not sure where to start or what Cats-mtl even is, please refer to the [getting started guide](https://typelevel.org/cats-mtl/getting-started.html).

### Laws

The **cats-mtl-laws** artifact provides [Discipline-style](https://github.com/typelevel/discipline) laws for all of the type classes defined in cats-mtl. It is relatively easy to use these laws to test your own implementations of these typeclasses. Take a look [here](https://github.com/typelevel/cats-mtl/tree/master/laws/src/main/scala/cats/mtl/laws) for more.

```scala
libraryDependencies += "org.typelevel" %% "cats-mtl-laws" % "0.6.0" % Test
```

These laws are compatible with both Specs2 and ScalaTest.

## Documentation

Links:

1. Website: [typelevel.org/cats-mtl/](https://typelevel.org/cats-mtl/)
2. ScalaDoc: [typelevel.org/cats-mtl/api/](https://typelevel.org/cats-mtl/api/)

Related Cats links (the core):

1. Website: [typelevel.org/cats/](https://typelevel.org/cats/)
2. ScalaDoc: [typelevel.org/cats/api/](https://typelevel.org/cats/api/)


## Migrating from Cats pre-1.0.0

cats-core used to provide various mtl-classes which were moved to cats-mtl and split up.
You can find the migration guide [here](https://typelevel.org/cats-mtl/migration)

### Community

People are expected to follow the
[Scala Code of Conduct](https://www.scala-lang.org/conduct/) when
discussing cats-mtl on the Github page, Gitter channel, or other
venues.

We hope that our community will be respectful, helpful, and kind. If
you find yourself embroiled in a situation that becomes heated, or
that fails to live up to our expectations, you should disengage and
contact one of the [project maintainers](#maintainers) in private. We
hope to avoid letting minor aggressions and misunderstandings escalate
into larger problems.

### License
All code is available to you under the MIT license, available at http://opensource.org/licenses/mit-license.php and also in the COPYING file. 
