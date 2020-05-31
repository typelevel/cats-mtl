---
layout: page
title:  "Getting Started"
section: "gettingstarted"
position: 2
---


## What is MTL?

MTL is an acronym and stands for Monad Transformer Library. 
Its main purpose is to make it easier to work with nested monad transformers. 
It achieves this by encoding the effects of most common monad transformers as type classes.

### The problem

Have you ever worked with two or more monad transformers nested inside each other?
If you haven't, working with what some call **monad transformer stacks** can be incredibly painful.
This is because, the more monad transformers you add to your stack the more type parameters the type system has to deal with and the worse type inference gets.
For example, here's a small example of a method that reads the current state in `StateT` and raises an error using `EitherT`: 

```scala mdoc
import cats.data._
import cats.implicits._

def checkState: EitherT[StateT[List, Int, ?], Exception, String] = for {
  currentState <- EitherT.liftF(StateT.get[List, Int])
  result <- if (currentState > 10) 
              EitherT.leftT[StateT[List, Int, ?], String](new Exception)
            else 
              EitherT.rightT[StateT[List, Int, ?], Exception]("All good")
} yield result
```

There's a bunch of type annotations and extra machinery that really has nothing to do with the actual program and this problem only gets worse as you add more expressions to your for-comprehension or add an additional monad transformer to the stack.

Thankfully, Cats-mtl is here to help.

## How Cats-mtl helps

### Monad Transformers encode some notion of *effect*

`EitherT` encodes the effect of short-circuiting errors.
`ReaderT` encodes the effect of reading a value from the environment.
`StateT` encodes the effect of pure local mutable state.

All of these monad transformers encode their effects as data structures, but there's another way to achieve the same result: Type classes!

For example take `ReaderT.ask` function, what would it look like if we used a type class here instead?
Well, Cats-mtl has an answer and it's called `ApplicativeAsk`.
You can think of it as `ReaderT` encoded as a type class:

```scala
trait ApplicativeAsk[F[_], E] {
  val applicative: Applicative[F]

  def ask: F[E]
}
```

At its core `ApplicativeAsk` just encodes the fact that we can ask for a value from the environment, exactly like `ReaderT` does.
Exactly like `ReaderT`, it also includes another type parameter `E`, that represents that environment.

If you're wondering why `ApplicativeAsk` has an `Applicative` field instead of just extending from `Applicative`, that is to avoid implicit ambiguities that arise from having multiple subclasses of a given type (here `Applicative`) in scope implicitly.
So in this case we favor composition over inheritance as otherwise, we could not e.g. use `Monad` together with `ApplicativeAsk`.

`ApplicativeAsk` is an example for what is at the core of Cats-mtl.
Cats-mtl provides type classes for most common effects which let you choose what kind of effects you need without commiting to a specific monad transformer stack.

Ideally, you'd write all your code using only an abstract type constructor `F[_]` with different type class constraints and then at the end run that code with a specific data type that is able to fulfill those constraints.
So without further ado, let's look at what the program from earlier looks when using Cats-mtl:

First, the original program again:

```scala mdoc:reset
import cats.data._
import cats.implicits._

def checkState: EitherT[StateT[List, Int, ?], Exception, String] = for {
  currentState <- EitherT.liftF(StateT.get[List, Int])
  result <- if (currentState > 10) 
              EitherT.leftT[StateT[List, Int, ?], String](new Exception("Too large"))
            else 
              EitherT.rightT[StateT[List, Int, ?], Exception]("All good")
} yield result
```

And now the mtl version:

```scala mdoc:reset
import cats.MonadError
import cats.implicits._
import cats.mtl.MonadState

def checkState[F[_]](implicit S: MonadState[F, Int], E: MonadError[F, Exception]): F[String] = for {
  currentState <- S.get
  result <- if (currentState > 10) E.raiseError(new Exception("Too large"))
            else E.pure("All good")
} yield result
```

We've reduced the boilerplate immensly!
Now our small program actually looks just like control flow and we didn't need to annotate any types.

This is great so far, but `checkState` now returns an abstract `F[String]`.
We need some `F` that has an instance of both `MonadState` and `MonadError`.
We know that `EitherT` can have a `MonadError` instance and `StateT` can have a `MonadState` instance, but how do we get both?
Fortunately, cats-mtl allows us to lift instances of mtl classes through our monad transformer stack.
That means, that e.g. `EitherT[StateT[List, Int, ?], Exception, A]` has both the `MonadError` and `MonadState` instances we need, neat!

So let's try turning our abstract `F[String]` into an actual value:

<!-- TODO make this back into an mdoc:silent; I had to remove it because of the deprecation of any2stringadd -->
```scala
val materializedProgram =
  checkState[EitherT[StateT[List, Int, ?], Exception, ?]]
```

This process of turning a program defined by an abstract type constructor with additional type class constraints into an actual concrete data type is sometimes called *interpreting* or *materializing*  a program.
Usually we don't have to do this until the very end where we want to run the full application, so the only place we see actual monad transformers is at the very edge.

In summary Cats-mtl provides two things:
MTL type classes representing effects and a way to lift instances of these classes through transformer stacks.

We suggest you start learning the MTL classes first and learn how lifting works later, as you don't need to understand lifting at all to enjoy all the benefits of Cats-mtl.

#### Available MTL classes

cats-mtl provides the following "MTL classes":
 - [ApplicativeAsk](mtl-classes/applicativeask.md)
 - [ApplicativeLocal](mtl-classes/applicativelocal.md)
 - [FunctorRaise](mtl-classes/functorraise.md)
 - [ApplicativeHandle](mtl-classes/applicativehandle.md)
 - [FunctorTell](mtl-classes/functortell.md)
 - [FunctorListen](mtl-classes/functorlisten.md)
 - [MonadState](mtl-classes/monadstate.md)
 - [MonadChronicle](mtl-classes/monadchronicle.md)

If you're wondering why some of these are based on `Monad` and others on `Functor` or `Applicative`, 
 it is because these are the smallest superclass dependencies practically possible with which you can define their laws.
This is in contrast to Haskell's `mtl` library and earlier versions of Cats as well as Scalaz,
 this gives us less restrictions over which type classes can be lifted over which transformers.

Because these type classes are commonly combined, the base typeclasses are ambiguous in implicit scope using
the typical cats subclass encoding via inheritance.
Thus in cats-mtl, the ambiguity is avoided by using the composition approach that we saw with `ApplicativeAsk` earlier.
In a future version of Scala we might be able to avoid this and have the same type class encoding everywhere.
