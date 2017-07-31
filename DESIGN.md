cats-mtl Design
===============
Overall, cats-mtl has similar design to cats: 
instances package, syntax package, implicits package.

There's also a hierarchy package, because 
cats-mtl uses a different typeclass encoding
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

## Lifting classes

This section incorporated from README.md.

Several typeclasses are provided to lift transformer typeclasses
through transformer data types, like `OptionT`.

They form a hierarchy:
```
MonadLayerControl
    \
    ||
    ||
    ||
    ||
    ||
    MonadLayerFunctor <----------- ApplicativeLayerFunctor <----------------- FunctorLayerFunctor
           \                                  \                                          \
           ||                                 ||                                         ||
           ||                                 ||                                         ||
           ||                                 ||                                         ||
           ||                                 ||                                         ||
           ||                                 ||                                         ||
           ||                                 ||                                         ||
           ||                                 ||                                         ||
           || MonadLayer <----------------    || ApplicativeLayer <-------------------   || FunctorLayer
           |\______________                    \______________                            \______________
```

`<X>Layer[M, Inner]` is three things:
 - an instance of `<X>[Inner]` and an instance of `<X>[M]`
 - a `FunctionK[Inner, M]` which respects the `<X>[Inner]` and `<X>[M]` instances
 - a higher-kinded variant of `cats.Invariant`, which maps
   `<X>`-isomorphisms in `Inner` (`(Inner ~> Inner, Inner ~> Inner)`) to
   `<X>`-homomorphisms in `M` (`M ~> M`)

It lets you lift monadic values into other monads which are "larger" than them,
for example monads that are being used inside monad transformers.
For example, `ApplicativeLocal` requires `MonadLayer` to lift through a transformer, because
all of its operations are invertible.

`<X>LayerFunctor` is a `<X>Layer` but instead a variant of `cats.Functor`, which maps
`<X>`-homomorphisms in `Inner` (`Inner ~> Inner`) to
`<X>`-homomorphisms in `M` (`M ~> M`).

Mapping natural transformations over the inner `Inner` inside `M`.

`MonadLayerControl` allows you to lift operations that *consume* `M` using the `layerControl` method,
which allows one the ability to "unravel" an `M[A]` to an `Inner[State[A]]` temporarily,
as long as the value produced with it is already in the `M[_]` context.
For example, `FunctorListen` requires this typeclass to lift through a transformer.

## Transformer classes
| still needs filling out |

### ApplicativeAsk / ApplicativeLocal
`MonadReader` from cats has been split into `ApplicativeAsk` and `ApplicativeLocal`;
the `Monad` constraint has been weakened to `Applicative`, and the `local` method was split out
into a subclass to widen the implementation space for `ApplicativeAsk`.

### FunctorTell / FunctorListen
Similarly to `MonadReader`, `MonadWriter` was split into `FunctorTell` and `FunctorListen`
and the constraint weakened to `Functor`, and `FunctorListen` being `FunctorTell` with the `listen`
method added.

