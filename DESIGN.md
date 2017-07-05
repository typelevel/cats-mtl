cats-mtl Design
===============
Overall, cats-mtl has similar design to cats: 
instances package, syntax package, implicits package.

There's also a hierarchy package, because 
cats-mtl uses a different type class encoding
than cats to avoid implicit ambiguity.

The problem looks like this:
```scala
import cats._

type Err
type Log

def f[F[_]: MonadError[?, Err]: MonadWriter[?, Log]]: Unit = {
      // implicitly[Monad[F]] // ambiguity between derived monads from MonadError and MonadWriter
      // 1.pure[F].flatMap(_ => 1.raise) // same ambiguity affects MonadSyntax
      ()
}
```

The cause is that the implicit conversions `MonadError[F, Err] => Monad[F]` and 
`MonadWriter[F, Log] => Monad[F]` are the same priority. If those conversions 
were different priorities, the issue would be fixed, and that's exactly what cats-mtl's encoding does.

Similarly to the [Scato](https://github.com/aloiscochard/scato) project, instead of inheritance
the transformer classes in cats-mtl use aggregation. Transformer classes thus contain instances
of their subclasses, and implicit defs taking the classes to their subclasses are prioritized explicitly
in the hierarchy package. This makes a-la-carte imports require an extra import, but it's worth having
multiple instances in scope at once.

MonadLayer, ApplicativeLayer, FunctorLayer
------------------------------------------
Given a type class `X[F[_]]`, `XLayer[Inner, M]` is the least constrained type class
that is used to lift `X`-based transformer type class instances from `Inner` to `M`; it contains instances of
`X[Inner]`, `X[M]`, and a higher-kinded homomorphism `Inner ~> M` which respects the operations of `X`.
As well, it is similar to a higher-kinded invariant functor with the operation 
`layerImapK[A](ma: M[A])(forward: Inner ~> Inner, backward: Inner ~> Inner): M[A]` 
which can be used to translate isomorphisms in `Inner` into endomorphisms in `M`. 
`X` is so far instantiated to `Monad`, `Applicative`, and `Functor`.

MonadLayerFunctor, ApplicativeLayerFunctor, FunctorLayerFunctor
---------------------------------------------------------------
Given a type class `X[F[_]]`, `XLayerFunctor[Inner, M]` is an `XLayer[Inner, M]`, and also like a higher-kinded
functor with the operation  `layerMapK[A](ma: M[A])(forward: Inner ~> Inner): M[A]` for lifting endomorphisms 
in `Inner` into endomorphisms in `M`.

MonadLayerControl
---------------------------------------------------------------
To be completed.

ApplicativeAsk / ApplicativeLocal
---------------------------------------------------------------
To be completed.

FunctorTell / FunctorListen
---------------------------------------------------------------
To be completed.

MonadLift
---------------------------------------------------------------
To be completed.
