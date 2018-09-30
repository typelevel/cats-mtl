---
layout: docs
title:  "MTL Type Classes"
section: "mtlclasses"
position: 3
---


## MTL classes summary

`ApplicativeAsk[F, E]` lets you access an `E` value in the `F[_]` context.
Intuitively, this means that an `E` value is required as an input to get "out" of the `F[_]` context.

`ApplicativeLocal[F, E]` extends `ApplicativeAsk` and lets you substitute all of the `ask` occurrences in an `F[A]` value with
`ask.map(f)`, for some `f: E => E` which produces a modified `E` input.

`FunctorRaise[F, E]` expresses the ability to raise errors of type `E` in a functorial `F[_]` context.
This means that a value of type `F[A]` may contain no `A` values but instead an `E` error value,
and further `map` calls will not have any values to execute the passed function on.

`ApplicativeHandle[F, E]` extends `FunctorRaise` with the ability to also handle raised errors.
It is fully equivalent to `cats.ApplicativeError`, but is not a subtype of `Applicative` and therefore doesn't cause any ambiguitites.

`FunctorTell[F, L]` is the ability to "log" values `L` inside a context `F[_]`, as an effect.

`FunctorListen[F, L]` extends `FunctorTell` and gives you the ability to expose the state
that is contained in all `F[A]` values, and that can be modified using `tell`. 
In that sense it is equivalent to a function `F[A] => F[(A, L)]`.

`MonadState[F, S]` is the capability to access and modify a state value
from inside the `F[_]` context, using `set(s: S): F[Unit]` and `get: F[S]`.

`MonadChronicle[F, E]` is the ability to both accumulate outputs and aborting computation with a final output.
In that sense it can be seen as a hybrid of `ApplicativeHandle` and `FunctorTell`.
