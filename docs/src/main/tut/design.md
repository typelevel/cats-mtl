---
layout: page
title:  "Design"
section: "design"
position: 6
---

cats-mtl Design
===============
Overall, cats-mtl has similar design to cats: 
instances package, syntax package, implicits package.

There's also a hierarchy package, because 
cats-mtl uses a different typeclass encoding
than cats to avoid implicit ambiguity.

The problem looks like this:
```scala
import cats._

type Err
type Log

def f[F[_]: MonadError[?, Err]: MonadWriter[?, Log]]: Unit = {
      // implicitly[Monad[F]] // ambiguity between derived monads from MonadError and MonadWriter
      // 1.pure[F].flatMap(_ => 1.raise) // same ambiguity affects MonadSyntax
      ()
}
```

The cause is that the implicit conversions `MonadError[F, Err] => Monad[F]` and 
`MonadWriter[F, Log] => Monad[F]` are the same priority. If those conversions 
were different priorities, the issue would be fixed, and that's exactly what cats-mtl's encoding does.

Similarly to the [Scato](https://github.com/aloiscochard/scato) project, instead of inheritance
the transformer classes in cats-mtl use aggregation. Mtl classes thus contain instances
of their superclasses.


## Motivation

The motivation for cats-mtl's existence can be summed up in a few points:
- using subtyping to express typeclass subclassing results in implicit ambiguities,
  and doing it another way would result in a massive inconsistency inside cats if only done for MTL classes.
  for a detailed explanation, see Adelbert Chang's article
  [here](http://typelevel.org/blog/2016/09/30/subtype-typeclasses.html).
- most MTL classes do not actually require `Monad` as a constraint for their laws.
  cats-mtl weakens this constraint to `Functor` or `Applicative` whenever possible,
  with the result that there's now a notion of a `Functor` transformer stack and 
  `Applicative` transformer stack in addition to that of a `Monad` transformer stack.
- the most used operations on `MonadWriter` and `MonadReader` are `tell` and `ask`,
  and the other operations severely restrict the space of implementations despite being
  used much less. To fix this `FunctorListen` and `ApplicativeLocal` are subclasses
  of `FunctorTell` and `ApplicativeAsk`, which have only the essentials.

The first point there means that it's impossible for cats-mtl type classes
to expose their base class instances implicitly; for example `F[_]: FunctorEmpty` isn't enough
for a `Functor[F]` to be visible in implicit scope, despite `FunctorEmpty` containing a `Functor`
instance as a member. The root cause here is that prioritizing implicit conversions with subtyping
explicitly can't work with cats and cats-mtl separate, as the `Functor[F]` instance for the type 
from cats will always conflict with a derived instance.

Thus `F[_]: FunctorFilter`, translated, becomes `F[_]: Functor: FunctorEmpty`.

For some historical info on the origins of cats-mtl, see:

[https://github.com/typelevel/cats/issues/1210](https://github.com/typelevel/cats/issues/1210)

[https://github.com/typelevel/cats/pull/1379](https://github.com/typelevel/cats/pull/1379)

[https://github.com/typelevel/cats/pull/1751](https://github.com/typelevel/cats/pull/1751)

## Laws

Type class laws come in a few varieties in cats-mtl: internal, external, and free.

Internal laws dictate how multiple operations inter-relate.
One side of the equation can always be reduced to a single function application of an operation.
These express "default" implementations that should be indistinguishable in result from the actual implementation.

External laws are laws that still need to be tested but don't fall into the internal laws.

Free laws are (in theory) unnecessary to test, because they are implied by other laws and the 
types of the operations in question. There will usually be rudimentary proofs 
or some justification attached to make sure these aren't just made up.

