---
layout: docs
title:  "Lifting Classes"
section: "lifting"
position: 4
---

## Lifting classes

Several typeclasses are provided to lift transformer typeclasses
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