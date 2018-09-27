---
layout: docs
title:  "MTL Type Classes"
section: "mtlclasses"
position: 3
---


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