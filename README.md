<img src="https://typelevel.org/cats-mtl/img/cats-mtl-logo.png" width="200px" height="231px" align="right">

## Cats MTL

Provides transformer typeclasses for cats' Monads, Applicatives and Functors.

You can have multiple cats-mtl transformer typeclasses in scope at once
without implicit ambiguity, unlike in pre-1.0.0 cats or Scalaz 7.

## Usage

```scala
libraryDependencies += "org.typelevel" %% "cats-mtl" % "1.4.0"
```

If your project uses ScalaJS, replace the double-`%` with a triple.  Note that **cats-mtl** has an upstream dependency on **cats-core** version 2.x.

Cross-builds are available for Scala 2.12, 2.13, 3, Scala Native, and ScalaJS major version 1.x.

If you're not sure where to start or what Cats MTL even is, please refer to the [getting started guide](https://typelevel.org/cats-mtl/getting-started.html).

### Supported Classes

- `EitherT`
- `Kleisli`
- `IorT`
- `OptionT`
- `ReaderWriterStateT`
- `StateT`
- `WriterT`

### Laws

The **cats-mtl-laws** artifact provides [Discipline-style](https://github.com/typelevel/discipline) laws for all of the type classes defined in cats-mtl. It is relatively easy to use these laws to test your own implementations of these typeclasses. Take a look [here](https://github.com/typelevel/cats-mtl/tree/main/laws/src/main/scala/cats/mtl/laws) for more.

```scala
libraryDependencies += "org.typelevel" %% "cats-mtl-laws" % "1.4.0" % Test
```

These laws are compatible with both Specs2 and ScalaTest.

## Documentation

Links:

1. Website: [typelevel.org/cats-mtl/](https://typelevel.org/cats-mtl/)
2. ScalaDoc: [typelevel.org/cats-mtl/api/](https://typelevel.org/cats-mtl/api/)

Related Cats links (the core):

1. Website: [typelevel.org/cats/](https://typelevel.org/cats/)
2. ScalaDoc: [typelevel.org/cats/api/](https://typelevel.org/cats/api/)


### Community

People are expected to follow the
[Typelevel Code of Conduct](https://typelevel.org/code-of-conduct) when
discussing cats-mtl on the Github page, Gitter channel, or other
venues.

We hope that our community will be respectful, helpful, and kind. If
you find yourself embroiled in a situation that becomes heated, or
that fails to live up to our expectations, you should disengage and
contact one of the project maintainers in private. We
hope to avoid letting minor aggressions and misunderstandings escalate
into larger problems.

### License
All code is available to you under the MIT license, available at http://opensource.org/licenses/mit-license.php and also in the COPYING file. 
