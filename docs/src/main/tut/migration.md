---
layout: page
title:  "Migration Guide"
section: "migration"
position: 5
---

#### Migration guide

Here's a map from pre-1.x cats typeclasses to cats-mtl typeclasses:
 - `MonadReader --> ApplicativeLocal`
 - `MonadWriter --> FunctorListen`
 - `MonadState --> MonadState`

cats typeclass parameters and context bounds have to be rewritten, to include base classes.
For example:
- `[F[_]: FunctorFilter]` will have to be adjusted to `[F[_]: Functor: FunctorEmpty]`,
- `[F[_]: MonadReader[?[_], E]]` will have to be adjusted to `F[_]: Monad: ApplicativeLocal[?[_], E]`

The root cause for this is addressed in the motivation section.


### ApplicativeAsk / ApplicativeLocal
`MonadReader` from cats has been split into `ApplicativeAsk` and `ApplicativeLocal`;
the `Monad` constraint has been weakened to `Applicative`, and the `local` method was split out
into a subclass to widen the implementation space for `ApplicativeAsk`.

### FunctorTell / FunctorListen
Similarly to `MonadReader`, `MonadWriter` was split into `FunctorTell` and `FunctorListen`
and the constraint weakened to `Functor`, and `FunctorListen` being `FunctorTell` with the `listen`
method added.
