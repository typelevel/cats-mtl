## cats-mtl

[![Build Status](https://api.travis-ci.org/typelevel/cats.svg)](https://travis-ci.org/typelevel/cats)

Transformer type classes for cats.

Provides transformer type classes for cats' Monads, Applicatives and Functors.

As well, there are some abstractions thrown in that allow you to
generically lift MTL type classes 
through transformers.

You can have multiple cats-mtl transformer type classes in scope at once 
without implicit ambiguity, unlike in pre-1.0.0 cats or Scalaz 7.

## Type classes

#### MTL classes

cats-mtl provides the following "MTL classes":
 - `ApplicativeAsk`
 - `ApplicativeLocal`
 - `FunctorEmpty`
 - `FunctorListen`
 - `FunctorRaise`
 - `FunctorTell`
 - `MonadState`

All of these are type classes built on top of other "base" type classes to provide extra laws.
Because they are commonly combined, the base type classes are ambiguous in implicit scope using
the typical cats subclass encoding via subtyping. Thus in cats-mtl, the ambiguity is avoided by using
an implicit scope with a different priority to house each conversion `Subclass => Base`.

#### Lifting classes

Several other type classes are provided to lift type classes 
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

It lets you lift monads into other monads which contain them, for example monads that are being used
inside monad transformers.
For example, `ApplicativeLocal` requires `MonadLayer` to lift through a transformer, because
all of its operations are invertible.

`<X>LayerFunctor` is a `<X>Layer` but instead a variant of `cats.Functor`, which maps
`<X>`-homomorphisms in `Inner` (`Inner ~> Inner`) to
`<X>`-homomorphisms in `M` (`M ~> M`).

Mapping natural transformations over the inner `Inner` inside `M`.

`MonadLayerControl` allows you to lift operations that *consume* `M` using the `layerControl` method,
which allows one the ability to "unravel" an `M[A]` to an `Inner[State[A]]` temporarily,
as long as the value produced with it is already in the `M[_]` context.
For example, `FunctorListen` requires this type class to lift through a transformer.

#### Why not MonadTrans, etc?
`MonadTrans` forces the shape `T[_[_], _]` on anything which can "contain" another monad, 
but newtyping around an instantiated transformer eliminates that shape.

## Type class summaries
From the scaladoc:

`ApplicativeAsk[F, E]` lets you access an `E` value in the `F[_]` context.
Intuitively, this means that an `E` value is required as an input to get "out" of the `F[_]` context.

`ApplicativeLocal[F, E]` lets you alter the `E` value that is observed by an `F[A]` value
using `ask`; the modification can only be observed from within that `F[A]` value.

`FunctorEmpty[F]` denotes the unique empty value of type `F[A]`.

`FunctorListen[F, L]` is a function `F[A] => F[(A, L)]` which exposes some state
that is contained in all `F[A]` values, and can be modified using `tell`.

`FunctorRaise[F, E]` expresses the ability to raise errors of type `E` in a functorial `F[_]` context.
This means that a value of type `F[A]` may contain no `A` values but instead an `E` error value,
and further `map` calls will not have any values to execute the passed function on.

`FunctorTell[F, L]` is the ability to "log" values `L` inside a context `F[_]`, as an effect.

`MonadState[F, S]` is the capability to access and modify a state value
from inside the `F[_]` context, using `set(s: S): F[Unit]` and `get: F[S]`.

## Laws

Laws come in a few loosely defined flavors in cats-mtl: internal, external, and free.

Internal laws dictate how multiple operations inter-relate, 
if either has a default implementation using the inherited operations of superclasses and the other operations.

External laws dictate how (potentially) multiple operations relate to each other and inherited operations,
but do not dictate a default implementation.

Free laws are (in theory) unnecessary to test, because they are implied by other laws and the 
types of the operations in question. There will usually be rudimentary proofs 
or some justification attached to make sure these aren't just made up.