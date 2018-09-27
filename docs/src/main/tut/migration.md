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