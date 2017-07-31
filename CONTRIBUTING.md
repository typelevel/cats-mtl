## Contributing

### Adding a new transformer typeclass

First thing is the set of operations, or the *signature*.
For example `FunctorTell[F, E]` has the signature `def tell(e: E): F[Unit]`.

Once you've got this down, you need to come up with a set of laws and a base typeclass.
These have to be worked out at the same time, because choosing base classes with more operations gives
you richer potential for laws but restricts implementations severely.

Come up with a rough sketch of the laws in Scala as methods but assuming no type parameters need to be
provided and all of the operations needed are in scope; use `<->` for the equality assertion.

To be continued.

### Adding a new transformer typeclass instance

The characteristic which transformer typeclasses have in common is that instances of them are
commonly lifted through transformer stacks. I.e., if you have an `ApplicativeAsk[E => ?]`, you can
have an `ApplicativeAsk[EitherT[E => ?, String, ?]]`; a so-called *inductive* instance of `ApplicativeAsk`,
because the capability is not provided by `EitherT` itself but the "inner monad".

Because of this, there's an argument to be made that there's only one transformer
typeclass instance you should need to provide: that of the "innermost" monad, the one
that is actually implementing the typeclass operations.