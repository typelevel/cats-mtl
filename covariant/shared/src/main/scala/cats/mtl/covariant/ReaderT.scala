package cats.mtl.covariant

import cats.data.Kleisli
import cats._
import cats.arrow.FunctionK

final class ReaderT[F[+_], -A, +B](val run: A => F[B]) {
  def ap[C, AA <: A](f: ReaderT[F, AA, B => C])(implicit F: Apply[F]): ReaderT[F, AA, C] =
    ReaderT(a => F.ap(f.run(a))(run(a)))

  /**
   * Performs [[local]] and [[map]] simultaneously.
   */
  def dimap[C, D](f: C => A)(g: B => D)(implicit F: Functor[F]): ReaderT[F, C, D] =
    ReaderT(c => F.map(run(f(c)))(g))

  /**
   * Modify the output of the ReaderT function with `f`.
   * {{{
   * scala> import cats.mtl.covariant.ReaderT, cats.implicits._
   * scala> val takeHead = ReaderT[Option, List[Int], Int](_.headOption)
   * scala> takeHead.map(_.toDouble).run(List(1))
   * res0: Option[Double] = Some(1.0)
   * }}}
   */
  def map[C](f: B => C)(implicit F: Functor[F]): ReaderT[F, A, C] =
    ReaderT(a => F.map(run(a))(f))

  def mapF[N[+_], C](f: F[B] => N[C]): ReaderT[N, A, C] =
    ReaderT(a => f(run(a)))

  /**
   * Modify the context `F` using transformation `f`.
   */
  def mapK[G[+_]](f: F ~> G): ReaderT[G, A, B] =
    ReaderT[G, A, B](a => f(run(a)))

  def flatMap[C, AA <: A](f: B => ReaderT[F, AA, C])(implicit F: FlatMap[F]): ReaderT[F, AA, C] =
    ReaderT.shift(a => F.flatMap[B, C](run(a))((b: B) => f(b).run(a)))

  def flatMapF[C](f: B => F[C])(implicit F: FlatMap[F]): ReaderT[F, A, C] =
    ReaderT.shift(a => F.flatMap(run(a))(f))

  /**
   * Composes [[run]] with a function `B => F[C]` not lifted into ReaderT.
   */
  def andThen[C](f: B => F[C])(implicit F: FlatMap[F]): ReaderT[F, A, C] =
    ReaderT.shift(a => F.flatMap(run(a))(f))

  /**
   * Tip to tail ReaderT arrow composition.
   * Creates a function `A => F[C]` from [[run]] (`A => F[B]`) and the given ReaderT of `B => F[C]`.
   * {{{
   * scala> import cats.mtl.covariant.ReaderT, cats.implicits._
   * scala> val takeHead = ReaderT[Option, List[Int], Int](_.headOption)
   * scala> val plusOne = ReaderT[Option, Int, Int](i => Some(i + 1))
   * scala> (takeHead andThen plusOne).run(List(1))
   * res0: Option[Int] = Some(2)
   * }}}
   */
  def andThen[C](k: ReaderT[F, B, C])(implicit F: FlatMap[F]): ReaderT[F, A, C] =
    this.andThen(k.run)

  def compose[Z, AA <: A](f: Z => F[AA])(implicit F: FlatMap[F]): ReaderT[F, Z, B] =
    ReaderT.shift((z: Z) => F.flatMap(f(z))(run))

  def compose[Z, AA <: A](k: ReaderT[F, Z, AA])(implicit F: FlatMap[F]): ReaderT[F, Z, B] =
    this.compose(k.run)

  def traverse[G[_], AA <: A, BB >: B](f: G[AA])(implicit F: Applicative[F], G: Traverse[G]): F[G[BB]] =
    G.traverse(f)(run)

  /**
   * Contramap the input using `f`, where `f` may modify the input type of the ReaderT arrow.
   * {{{
   * scala> import cats.mtl.covariant.ReaderT, cats.implicits._
   * scala> type ParseResult[A] = Either[Throwable, A]
   * scala> val parseInt = ReaderT[ParseResult, String, Int](s => Either.catchNonFatal(s.toInt))
   * scala> parseInt.local[List[String]](_.combineAll).run(List("1", "2"))
   * res0: ParseResult[Int] = Right(12)
   * }}}
   */
  def local[AA](f: AA => A): ReaderT[F, AA, B] =
    ReaderT(aa => run(f(aa)))

  def lower(implicit F: Applicative[F]): ReaderT[F, A, F[B]] =
    ReaderT(a => F.pure(run(a)))

  def first[C](implicit F: Functor[F]): ReaderT[F, (A, C), (B, C)] =
    ReaderT { case (a, c) => F.fproduct(run(a))(_ => c) }

  def second[C](implicit F: Functor[F]): ReaderT[F, (C, A), (C, B)] =
    ReaderT { case (c, a) => F.map(run(a))(c -> _) }

  /** Discard computed B and yield the input value. */
  def tap[AA <: A](implicit F: Functor[F]): ReaderT[F, AA, AA] =
    ReaderT(a => F.as(run(a), a))

  /** Yield computed B combined with input value. */
  def tapWith[C, AA <: A](f: (AA, B) => C)(implicit F: Functor[F]): ReaderT[F, AA, C] =
    ReaderT(a => F.map(run(a))(b => f(a, b)))

  def tapWithF[C, AA <: A](f: (AA, B) => F[C])(implicit F: FlatMap[F]): ReaderT[F, AA, C] =
    ReaderT(a => F.flatMap(run(a))(b => f(a, b)))


  def apply(a: A): F[B] = run(a)
  
  def toKleisli[BB >: B]: Kleisli[F, A, BB] =
    Kleisli(a => run(a))
}

object ReaderT {
  def apply[F[+_], A, B](value: A => F[B]): ReaderT[F, A, B] =
    ReaderT(value)

    /**
   * Internal API — shifts the execution of `run` in the `F` context.
   *
   * Used to build ReaderT values for `F[_]` data types that implement `Monad`,
   * in which case it is safer to trigger the `F[_]` context earlier.
   *
   * The requirement is for `FlatMap` as this will get used in operations
   * that invoke `F.flatMap` (e.g. in `ReaderT#flatMap`). However we are
   * doing discrimination based on inheritance and if we detect an
   * `Applicative`, then we use it to trigger the `F[_]` context earlier.
   *
   * Triggering the `F[_]` context earlier is important to avoid stack
   * safety issues for `F` monads that have a stack safe `flatMap`
   * implementation. For example `Eval` or `IO`. Without this the `Monad`
   * instance is stack unsafe, even if the underlying `F` is stack safe
   * in `flatMap`.
   */
  private[covariant] def shift[F[+_], A, B](run: A => F[B])(implicit F: FlatMap[F]): ReaderT[F, A, B] =
    F match {
      case ap: Applicative[F] @unchecked =>
        ReaderT(r => F.flatMap(ap.pure(r))(run))
      case _ =>
        ReaderT(run)
    }

  implicit def catsMtlCovariantReaderTMonad[F[+_]: Monad, A]: Monad[ReaderT[F, A, *]] = new Monad[ReaderT[F, A, *]] {
    def flatMap[B, C](fa: ReaderT[F,A,B])(f: B => ReaderT[F,A,C]): ReaderT[F,A,C] = 
      fa.flatMap(f)
    
    def tailRecM[B, C](b: B)(f: B => ReaderT[F,A,Either[B,C]]): ReaderT[F,A,C] = 
      ReaderT[F, A, C]({ a =>
        Monad[F].tailRecM(b) { f(_).run(a) }
      })
    
    def pure[B](x: B): ReaderT[F,A,B] = ReaderT(_ => Monad[F].pure(x))
    
  }
}

sealed private[covariant] class ReaderTFunctions {
  /**
   * Creates a ReaderT that ignores its input `A` and returns the given `F[B]`.
   * {{{
   * scala> import cats.mtl.covariant.ReaderT, cats.implicits._
   * scala> val takeHead = ReaderT((_:List[Int]).headOption)
   * scala> val makeList = ReaderT.liftF[Option, Unit, List[Int]](Some(List(1,2,3)))
   * scala> (makeList andThen takeHead).run(())
   * res0: Option[Int] = Some(1)
   * }}}
   */
  def liftF[F[+_], A, B](x: F[B]): ReaderT[F, A, B] =
    ReaderT(_ => x)

  /**
   * Same as [[liftF]], but expressed as a FunctionK for use with mapK
   * {{{
   * scala> import cats._, mtl.covariant._, implicits._
   * scala> val a: OptionT[Eval, Int] = 1.pure[OptionT[Eval, *]]
   * scala> val b: OptionT[ReaderT[Eval, String, *], Int] = a.mapK(ReaderT.liftK)
   * scala> b.value.run("").value
   * res0: Option[Int] = Some(1)
   * }}}
   */
  def liftK[F[+_], A]: F ~> ReaderT[F, A, *] =
    new (F ~> ReaderT[F, A, *]) {
      def apply[B](fa: F[B]): ReaderT[F,A,B] = liftF(fa)
    }


  /**
   * Creates a ReaderT arrow ignoring its input and lifting the given `B` into applicative context `F`.
   * {{{
   * scala> import cats.mtl.covariant.ReaderT, cats.implicits._
   * scala> val pureOpt = ReaderT.pure[Option, Unit, String]("beam me up!")
   * scala> pureOpt.run(())
   * res0: Option[String] = Some(beam me up!)
   * }}}
   */
  def pure[F[+_], A, B](x: B)(implicit F: Applicative[F]): ReaderT[F, A, B] =
    ReaderT(_ => F.pure(x))

  /**
   * Creates a ReaderT arrow which can lift an `A` into applicative context `F`.
   * This is distinct from [[pure]] in that the input is what is lifted (and not ignored).
   * {{{
   * scala> ReaderT.ask[Option, Int].run(1)
   * res0: Option[Int]: Some(1)
   * }}}
   */
  def ask[F[+_], A](implicit F: Applicative[F]): ReaderT[F, A, A] =
    ReaderT(F.pure)

  /**
   * Modifies the input environment with `f`, without changing the input type of the ReaderT.
   * {{{
   * scala> import cats.mtl.covariant.ReaderT
   * scala> val takeHead = ReaderT[Option, List[Int], Int](_.headOption)
   * scala> ReaderT.local[Option, Int, List[Int]](1 :: _)(takeHead).run(List(2,3))
   * res0: Option[Int] = Some(1)
   * }}}
   */
  def local[M[+_], A, R](f: R => R)(fa: ReaderT[M, R, A]): ReaderT[M, R, A] =
    ReaderT(r => fa.run(f(r)))

    /**
   * Lifts a natural transformation of effects within a ReaderT
   * to a transformation of ReaderTs.
   *
   * Equivalent to running `mapK(f) on a ReaderT.
   *
   * {{{
   * scala> import cats._, mtl.covariant._
   * scala> val f: (List ~> Option) = λ[List ~> Option](_.headOption)
   *
   * scala> val k: ReaderT[List, String, Char] = ReaderT(_.toList)
   * scala> k.run("foo")
   * res0: List[Char] = List(f, o, o)
   *
   * scala> val k2: ReaderT[Option, String, Char] = ReaderT.liftFunctionK(f)(k)
   * scala> k2.run("foo")
   * res1: Option[Char] = Some(f)
   * }}}
   * */
  def liftFunctionK[F[+_], G[+_], A](f: F ~> G): ReaderT[F, A, *] ~> ReaderT[G, A, *] =
    new (ReaderT[F, A, *] ~> ReaderT[G, A, *]) {
      def apply[B](fa: ReaderT[F,A,B]): ReaderT[G,A,B] = fa.mapK(f)
    }
}