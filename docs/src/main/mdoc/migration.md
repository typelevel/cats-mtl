---
layout: page
title:  "Migration Guide"
section: "migration"
position: 5
---

## Migration guide

Here's a map from pre-1.x cats typeclasses to cats-mtl typeclasses:
 - `MonadReader --> Local`
 - `MonadWriter --> Listen`
 - `Stateful --> Stateful`

cats typeclass parameters and context bounds have to be rewritten, to include base classes.
For example:
 - `[F[_]: MonadReader[?[_], E]]` will have to be adjusted to `[F[_]: Monad: Local[?[_], E]]`,
 - `[F[_]: MonadWriter[?[_], L]]` will have to be adjusted to `[F[_]: Monad: Listen[?[_], L]]`,
 - `[F[_]: Stateful[?[_], S]]` will have to be adjusted to `[F[_]: Monad: Stateful[?[_], S]]`

The root cause for this is addressed in the motivation section.


### Ask / Local
`MonadReader` from cats has been split into `Ask` and `Local`;
the `Monad` constraint has been weakened to `Applicative`, and the `local` method was split out
into a subclass to widen the implementation space for `Ask`.

### Tell / Listen
Similarly to `MonadReader`, `MonadWriter` was split into `Tell` and `Listen`
and the constraint weakened to `Functor`, and `Listen` being `Tell` with the `listen`
method added.
