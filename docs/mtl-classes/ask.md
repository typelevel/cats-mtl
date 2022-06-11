## Ask

`Ask[F, E]` allows us to read a value of type `E` from a shared environment into a context `F[_]`.
In practical terms, this means, that if we have an instance of `Ask[F, E]`,
 we can now request a value of `F[E]` by using the `ask` method.

Simplified, it is defined like this:

```scala mdoc
trait Ask[F[_], E] {
  def ask: F[E]
}
```

This typeclass comes in handy whenever we want to be able to request a value from the environment,
 e.g. read from some external configuration.

### Instances

A trivial instance for `Ask` can be just a plain function:

```scala mdoc
def functionAsk[E]: Ask[E => *, E] = new Ask[E => *, E] {
  def ask: E => E = identity
}
```

Other instances include `Reader` and `ReaderT`/`Kleisli`.
In fact the implementation for `ask` is just `ReaderT.ask`, so it's a perfect fit.

Cats-mtl is able to lift `Ask` instances through a monad transformer stack, so for example,
`StateT[Reader[E, *], S, A]` and `EitherT[ReaderT[List, E, *], Error, A]` will both have `Ask` instances whenever you import from  `cats.mtl.instances._`.

In general every monad transformer stack where `Reader` appears once, will have an instance of `Ask`.
