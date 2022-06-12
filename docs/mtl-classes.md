## MTL classes summary

`Tell[F, L]` is the ability to "log" values `L` inside a context `F[_]`, as an effect.
The primary monad transformer instance for `Tell` is `WriterT`.

`Listen[F, L]` extends `Tell` and gives you the ability to expose the state
that is contained in all `F[A]` values, and that can be modified using `tell`.
In that sense it is equivalent to a function `F[A] => F[(A, L)]`.
The primary monad transformer instance for `Listen` is `WriterT`.

`Ask[F, E]` lets you access an `E` value in the `F[_]` context.
Intuitively, this means that an `E` value is required as an input to get "out" of the `F[_]` context.
The primary monad transformer instance for `Ask` is `ReaderT`.

`Local[F, E]` extends `Ask` and lets you substitute all of the `ask` occurrences in an `F[A]` value with
`ask.map(f)`, for some `f: E => E` which produces a modified `E` input.
The primary monad transformer instance for `Local` is `ReaderT`.

`Stateful[F, S]` is the capability to access and modify a state value
from inside the `F[_]` context, using `set(s: S): F[Unit]` and `get: F[S]`.
The primary monad transformer instance for `Stateful` is `StateT`.

`Chronicle[F, E]` is the ability to both accumulate outputs and aborting computation with a final output.
In that sense it can be seen as a hybrid of `Handle` and `Tell`.
The primary monad transformer instance for `Chronicle` is `IorT`.

`Raise[F, E]` expresses the ability to raise errors of type `E` in a functorial `F[_]` context.
This means that a value of type `F[A]` may contain no `A` values but instead an `E` error value,
and further `map` calls will not have any values to execute the passed function on.
The primary monad transformer instance for `Raise` is `EitherT`.

`Handle[F, E]` extends `Raise` with the ability to also handle raised errors.
It is fully equivalent to `cats.ApplicativeError`, but is not a subtype of `Applicative` and therefore doesn't cause any ambiguitites.
The primary monad transformer instance for `Handle` is `EitherT`.
