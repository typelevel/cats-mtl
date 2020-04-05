package cats.mtl.covariant

import cats._
import cats.data.Nested
import cats.instances.option.{catsStdInstancesForOption => optionInstance}

final class OptionT[F[+_], +A](val value: F[Option[A]]) {

  def fold[B](default: => B)(f: A => B)(implicit F: Functor[F]): F[B] =
    F.map(value)(_.fold(default)(f))

  /**
   * Catamorphism on the Option. This is identical to [[fold]], but it only has
   * one parameter list, which can result in better type inference in some
   * contexts.
   */
  def cata[B](default: => B, f: A => B)(implicit F: Functor[F]): F[B] =
    fold(default)(f)

  def map[B](f: A => B)(implicit F: Functor[F]): OptionT[F, B] =
    OptionT(F.map(value)(_.map(f)))

  /**
   * Modify the context `F` using transformation `f`.
   */
  def mapK[G[+_]](f: F ~> G): OptionT[G, A] = OptionT[G, A](f(value))

  def semiflatMap[B](f: A => F[B])(implicit F: Monad[F]): OptionT[F, B] =
    flatMap(a => OptionT.liftF(f(a)))

  def mapFilter[B](f: A => Option[B])(implicit F: Functor[F]): OptionT[F, B] =
    subflatMap(f)

  def flatMap[B](f: A => OptionT[F, B])(implicit F: Monad[F]): OptionT[F, B] =
    flatMapF(a => f(a).value)

  def flatMapF[B](f: A => F[Option[B]])(implicit F: Monad[F]): OptionT[F, B] =
    OptionT(F.flatMap(value)(_.fold(F.pure[Option[B]](None))(f)))

  def flatTransform[B](f: Option[A] => F[Option[B]])(implicit F: Monad[F]): OptionT[F, B] =
    OptionT(F.flatMap(value)(f))

  def transform[B](f: Option[A] => Option[B])(implicit F: Functor[F]): OptionT[F, B] =
    OptionT(F.map(value)(f))

  def subflatMap[B](f: A => Option[B])(implicit F: Functor[F]): OptionT[F, B] =
    transform(_.flatMap(f))

  def getOrElse[B >: A](default: => B)(implicit F: Functor[F]): F[B] =
    F.map(value)(_.getOrElse(default))

  def getOrElseF[B >: A](default: => F[B])(implicit F: Monad[F]): F[B] =
    F.flatMap(value)(_.fold(default)(F.pure))

  def collect[B](f: PartialFunction[A, B])(implicit F: Functor[F]): OptionT[F, B] =
    OptionT(F.map(value)(_.collect(f)))

  def exists(f: A => Boolean)(implicit F: Functor[F]): F[Boolean] =
    F.map(value)(_.exists(f))

  def filter(p: A => Boolean)(implicit F: Functor[F]): OptionT[F, A] =
    OptionT(F.map(value)(_.filter(p)))

  def withFilter(p: A => Boolean)(implicit F: Functor[F]): OptionT[F, A] =
    filter(p)(F)

  def filterNot(p: A => Boolean)(implicit F: Functor[F]): OptionT[F, A] =
    OptionT(F.map(value)(_.filterNot(p)))

  def forall(f: A => Boolean)(implicit F: Functor[F]): F[Boolean] =
    F.map(value)(_.forall(f))

  def isDefined(implicit F: Functor[F]): F[Boolean] =
    F.map(value)(_.isDefined)

  def isEmpty(implicit F: Functor[F]): F[Boolean] =
    F.map(value)(_.isEmpty)

  def orElse[AA >: A](default: => OptionT[F, AA])(implicit F: Monad[F]): OptionT[F, AA] =
    orElseF(default.value)

  def orElseF[AA >: A](default: => F[Option[AA]])(implicit F: Monad[F]): OptionT[F, AA] =
    OptionT(F.flatMap(value) {
      case s @ Some(_) => F.pure(s)
      case None        => default
    })

  def toRight[L](left: => L)(implicit F: Functor[F]): EitherT[F, L, A] =
    EitherT(cata(Left(left), Right.apply))

  def toLeft[R](right: => R)(implicit F: Functor[F]): EitherT[F, A, R] =
    EitherT(cata(Right(right), Left.apply))

  def show[AA >: A](implicit F: Show[F[Option[AA]]]): String = F.show(value)

  def compare[AA >: A](that: OptionT[F, AA])(implicit o: Order[F[Option[AA]]]): Int =
    o.compare(value, that.value)

  def partialCompare[AA >: A](that: OptionT[F, AA])(implicit p: PartialOrder[F[Option[AA]]]): Double =
    p.partialCompare(value, that.value)

  def ===[AA >: A](that: OptionT[F, AA])(implicit eq: Eq[F[Option[AA]]]): Boolean =
    eq.eqv(value, that.value)

  def traverse[G[_], B](f: A => G[B])(implicit F: Traverse[F], G: Applicative[G]): G[OptionT[F, B]] =
    G.map(F.compose(optionInstance).traverse(value)(f))(OptionT.apply)

  def foldLeft[B](b: B)(f: (B, A) => B)(implicit F: Foldable[F]): B =
    F.compose(optionInstance).foldLeft(value, b)(f)

  def foldRight[B](lb: Eval[B])(f: (A, Eval[B]) => Eval[B])(implicit F: Foldable[F]): Eval[B] =
    F.compose(optionInstance).foldRight(value, lb)(f)

  /**
   * Transform this `OptionT[F, A]` into a `[[Nested]][F, Option, A]`.
   *
   * An example where `toNested` can be used, is to get the `Apply.ap` function with the
   * behavior from the composed `Apply` instances from `F` and `Option`, which is
   * inconsistent with the behavior of the `ap` from `Monad` of `OptionT`.
   *
   * {{{
   * scala> import cats.implicits._
   * scala> import cats.mtl.covariant.OptionT
   * scala> val ff: OptionT[List, Int => String] =
   *      |   OptionT(List(Option(_.toString), None))
   * scala> val fa: OptionT[List, Int] = OptionT(List(Option(1), Option(2)))
   * scala> ff.ap(fa)
   * res0: OptionT[List,String] = OptionT(List(Some(1), Some(2), None))
   * scala> OptionT(ff.toNested.ap(fa.toNested).value)
   * res1: OptionT[List,String] = OptionT(List(Some(1), Some(2), None, None))
   * }}}
   */
  def toNested[AA >: A]: Nested[F, Option, AA] = Nested(value)
}

object OptionT {
  def apply[F[+_], A](value: F[Option[A]]): OptionT[F, A] =
    new OptionT(value)

  /**
   * Uses the [[http://typelevel.org/cats/guidelines.html#partially-applied-type-params Partially Applied Type Params technique]] for ergonomics.
   */
  final private[covariant] class PurePartiallyApplied[F[+_]](private val dummy: Boolean = true) extends AnyVal {
    def apply[A](value: A)(implicit F: Applicative[F]): OptionT[F, A] =
      OptionT(F.pure(Some(value)))
  }

  /** Creates a `OptionT[A]` from an `A`
   *
   * {{{
   * scala> import cats.implicits._
   * scala> OptionT.pure[List](2)
   * res0: OptionT[List, Int] = OptionT(List(Some(2)))
   * }}}
   *
   */
  def pure[F[+_]]: PurePartiallyApplied[F] = new PurePartiallyApplied[F]

  /** An alias for pure
   *
   * {{{
   * scala> import cats.implicits._
   * scala> OptionT.some[List](2)
   * res0: OptionT[List, Int] = OptionT(List(Some(2)))
   * }}}
   *
   */
  def some[F[+_]]: PurePartiallyApplied[F] = pure

  def none[F[+_], A](implicit F: Applicative[F]): OptionT[F, A] =
    OptionT(F.pure(None))

  /**
   * Transforms an `Option` into an `OptionT`, lifted into the specified `Applicative`.
   *
   * {{{
   * scala> import cats.implicits._
   * scala> val o: Option[Int] = Some(2)
   * scala> OptionT.fromOption[List](o)
   * res0: OptionT[List, Int] = OptionT(List(Some(2)))
   * }}}
   */
  def fromOption[F[+_]]: FromOptionPartiallyApplied[F] = new FromOptionPartiallyApplied

  /**
   * Uses the [[http://typelevel.org/cats/guidelines.html#partially-applied-type-params Partially Applied Type Params technique]] for ergonomics.
   */
  final private[covariant] class FromOptionPartiallyApplied[F[+_]](private val dummy: Boolean = true) extends AnyVal {
    def apply[A](value: Option[A])(implicit F: Applicative[F]): OptionT[F, A] =
      OptionT(F.pure(value))
  }

  /**
   * Lifts the `F[A]` Functor into an `OptionT[F, A]`.
   */
  def liftF[F[+_], A](fa: F[A])(implicit F: Functor[F]): OptionT[F, A] = OptionT(F.map(fa)(Some(_)))

  /**
   * Same as [[liftF]], but expressed as a FunctionK for use with mapK
   * {{{
   * scala> import cats._, mtl.covariant._,  implicits._
   * scala> val a: EitherT[Eval, String, Int] = 1.pure[EitherT[Eval, String, *]]
   * scala> val b: EitherT[OptionT[Eval, *], String, Int] = a.mapK(OptionT.liftK)
   * scala> b.value.value.value
   * res0: Option[Either[String,Int]] = Some(Right(1))
   * }}}
   */
  def liftK[F[+_]](implicit F: Functor[F]): F ~> OptionT[F, *] =
    new (F ~> OptionT[F, *]) { 
      def apply[A](fa: F[A]): OptionT[F,A] = liftF(fa)
    }
}