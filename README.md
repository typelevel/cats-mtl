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

## SBT
```scala
libraryDependencies += "org.typelevel" %%% "cats-mtl-core" % "0.3.0"
```

## Typeclasses

#### Migration guide

Here's a map from pre-1.x cats typeclasses to cats-mtl typeclasses:
 - `MonadReader --> ApplicativeLocal`
 - `MonadWriter --> FunctorListen`
 - `MonadState --> MonadState`
 - `FunctorFilter --> FunctorEmpty`
 - `TraverseFilter --> TraverseEmpty`

cats typeclass parameters and context bounds have to be rewritten, to include base classes.
For example:
- `[F[_]: FunctorFilter]` will have to be adjusted to `[F[_]: Functor: FunctorEmpty]`,
- `[F[_]: MonadReader[?[_], E]]` will have to be adjusted to `F[_]: Monad: ApplicativeLocal[?[_], E]`

The root cause for this is addressed in the motivation section.

#### MTL classes

cats-mtl provides the following "MTL classes":
 - `ApplicativeAsk`
 - `ApplicativeLocal`
 - `FunctorEmpty`
 - `FunctorListen`
 - `FunctorRaise`
 - `FunctorTell`
 - `MonadState`

All of these are typeclasses built on top of other "base" typeclasses to provide extra laws.
Because they are commonly combined, the base typeclasses are ambiguous in implicit scope using
the typical cats subclass encoding via subtyping. Thus in cats-mtl, the ambiguity is avoided by using
an implicit scope with a different priority to house each conversion `Subclass => Base`.

Compared to cats and Haskell's mtl library, cats-mtl's typeclasses have the smallest superclass dependencies
practically possible and in some cases smaller sets of operations so that they can be lifted over more transformers.

#### Lifting classes

Several other typeclasses are provided to lift typeclasses
through transformer data types, like `OptionT`.

They form a hierarchy:
```
MonadLayerControl
    \
    ||
    ||
    ||
    ||
    ||
    MonadLayerFunctor <----------- ApplicativeLayerFunctor <----------------- FunctorLayerFunctor
           \                                  \                                          \
           ||                                 ||                                         ||
           ||                                 ||                                         ||
           ||                                 ||                                         ||
           ||                                 ||                                         ||
           ||                                 ||                                         ||
           ||                                 ||                                         ||
           ||                                 ||                                         ||
           || MonadLayer <----------------    || ApplicativeLayer <-------------------   || FunctorLayer
           |\______________                    \______________                            \______________
```

`<X>Layer[M, Inner]` is three things:
 - an instance of `<X>[Inner]` and an instance of `<X>[M]`
 - a `FunctionK[Inner, M]` which respects the `<X>[Inner]` and `<X>[M]` instances
 - a higher-kinded variant of `cats.Invariant`, which maps
   `<X>`-isomorphisms in `Inner` (`(Inner ~> Inner, Inner ~> Inner)`) to
   `<X>`-homomorphisms in `M` (`M ~> M`)

It lets you lift monadic values into other monads which are "larger" than them,
for example monads that are being used inside monad transformers.
For example, `ApplicativeLocal` requires `MonadLayer` to lift through a transformer, because
all of its operations are invertible.

`<X>LayerFunctor` is a `<X>Layer` but instead a variant of `cats.Functor`, which maps
`<X>`-homomorphisms in `Inner` (`Inner ~> Inner`) to
`<X>`-homomorphisms in `M` (`M ~> M`).

Mapping natural transformations over the inner `Inner` inside `M`.

`MonadLayerControl` allows you to lift operations that *consume* `M` using the `layerControl` method,
which allows one the ability to "unravel" an `M[A]` to an `Inner[State[A]]` temporarily,
as long as the value produced with it is already in the `M[_]` context.
For example, `FunctorListen` requires this typeclass to lift through a transformer.

#### Why not MonadTrans, etc?
`MonadTrans` forces the shape `T[_[_], _]` on anything which can "contain" another monad,
but newtyping around an instantiated transformer eliminates that shape. I don't see usecases
in the wild which lift transformations `M ~> N` inside a transformer to implement any MTL class,
so the extra power was removed.

#### Typeclass summaries
From the scaladoc:

`ApplicativeAsk[F, E]` lets you access an `E` value in the `F[_]` context.
Intuitively, this means that an `E` value is required as an input to get "out" of the `F[_]` context.

`ApplicativeLocal[F, E]` lets you substitute all of the `ask` occurrences in an `F[A]` value with
`ask.map(f)`, for some `f: E => E` which produces a modified `E` input.

`FunctorEmpty[F]` allows you to `map` and filter out elements simultaneously.

`FunctorListen[F, L]` is a function `F[A] => F[(A, L)]` which exposes some state
that is contained in all `F[A]` values, and can be modified using `tell`.

`FunctorRaise[F, E]` expresses the ability to raise errors of type `E` in a functorial `F[_]` context.
This means that a value of type `F[A]` may contain no `A` values but instead an `E` error value,
and further `map` calls will not have any values to execute the passed function on.

`FunctorTell[F, L]` is the ability to "log" values `L` inside a context `F[_]`, as an effect.

`MonadState[F, S]` is the capability to access and modify a state value
from inside the `F[_]` context, using `set(s: S): F[Unit]` and `get: F[S]`.

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

https://github.com/typelevel/cats/issues/1210

https://github.com/typelevel/cats/pull/1379

https://github.com/typelevel/cats/pull/1751

## Laws

Type class laws come in a few varieties in cats-mtl: internal, external, and free.

Internal laws dictate how multiple operations inter-relate.
One side of the equation can always be reduced to a single function application of an operation.
These express "default" implementations that should be indistinguishable in result from the actual implementation.

External laws are laws that still need to be tested but don't fall into the internal laws.

Free laws are (in theory) unnecessary to test, because they are implied by other laws and the 
types of the operations in question. There will usually be rudimentary proofs 
or some justification attached to make sure these aren't just made up.

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

### License
All code is available to you under the MIT license, available at http://opensource.org/licenses/mit-license.php and also in the COPYING file. 
