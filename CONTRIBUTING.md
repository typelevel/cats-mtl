## Contributing

### Adding a new MTL type class

First thing is the set of operations, or the *signature*.
For example `FunctorTell[F, E]` has the signature `def tell(e: E): F[Unit]`.

Once you've got this down, you need to come up with a set of laws and a base type class.
These have to be worked out at the same time, because choosing base classes with more operations gives
you richer potential for laws but restricts implementations severely.

Come up with a rough sketch of the laws in Scala as methods but assuming no type parameters need to be provided 
and all of the operations needed are in scope; use `<->` for the equality assertion.

To be continued.

### Adding a new MTL type class instance

To be filled in.