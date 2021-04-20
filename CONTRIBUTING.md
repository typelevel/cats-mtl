## Contributing

### Adding a new transformer typeclass

First thing is the set of operations, or the *signature*.
For example `Tell[F, E]` has the signature `def tell(e: E): F[Unit]`.

Once you've got this down, you need to come up with a set of laws and a base typeclass.
These have to be worked out at the same time, because choosing base classes with more operations gives
you richer potential for laws but restricts implementations severely.

Come up with a rough sketch of the laws in Scala as methods but assuming no type parameters need to be
provided and all of the operations needed are in scope; use `<->` for the equality assertion.

To be continued.

### Adding a new transformer typeclass instance

The characteristic which transformer typeclasses have in common is that instances of them are
commonly lifted through transformer stacks. I.e., if you have an `Ask[E => ?]`, you can
have an `Ask[EitherT[E => ?, String, ?]]`; a so-called *inductive* instance of `Ask`,
because the capability is not provided by `EitherT` itself but the "inner monad".

Because of this, there's an argument to be made that there's only one transformer
typeclass instance you should need to provide: that of the "innermost" monad, the one
that is actually implementing the typeclass operations.


### Running Documentation Site

The cats-mtl website runs on [SBT-Microsite](https://47degrees.github.io/sbt-microsites/). 
To generate the website locally, run `sbt`

```
$ sbt
```

This will take you to the `root` project. 

Next, run `makeMicrosite` within sbt

```
sbt:root> makeMicrosite
```

Once complete, this will create a _docs/target/site_ directory. Ensure that you have 
[jekyll](https://jekyllrb.com/) installed. In another terminal, go to the
_cats-mtl/docs/target/site_ directory, and run in your shell (not sbt):

```
$ jekyll serve -b /cats-mtl
```

Browse to `http://127.0.0.1:4000/cats-mtl/` as directed by `jekyll`. The `-b /cats-mtl` is 
required so that context-path is correct and all resources are resolved correctly.

Keep in mind that you can keep `jekyll` running while you make continuing calls to `makeMicrosite` in sbt. 
`jekyll` will watch and rebuild continually as you make changes.
