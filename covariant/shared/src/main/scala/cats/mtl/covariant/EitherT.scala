package cats.mtl.covariant

import cats.data.{Nested, ValidatedNel, Validated, ValidatedNec}
import cats._
import cats.syntax.either._
import cats.syntax.functor._
import cats.instances.either._

final class EitherT[F[+_], +A, +B](val value: F[Either[A, B]]) {
  def toCatsData[AA >: A, BB >: B]: cats.data.EitherT[F, AA, BB] =
    cats.data.EitherT(value)

  /** Transform this `EitherT[F, A, B]` into a `F[C]`.
   *
   * Example:
   * {{{
   * scala> import cats.implicits._
   * scala> import cats.mtl.covariant.EitherT
   *
   * scala> val eitherT: EitherT[List, String, Int] = EitherT[List, String, Int](List(Left("456"), Right(123)))
   * scala> eitherT.fold(string => string.toInt, int => int)
   * res0: List[Int] = List(456, 123)
   * }}}
   */
  def fold[C](fa: A => C, fb: B => C)(implicit F: Functor[F]): F[C] = F.map(value)(_.fold(fa, fb))

  /** Transform this `EitherT[F, A, B]` into a `F[C]`.
   *
   * Example:
   * {{{
   * scala> import cats.implicits._
   * scala> import cats.mtl.covariant.EitherT
   *
   * scala> val eitherT: EitherT[List, String, Int] = EitherT[List, String, Int](List(Right(123),Left("abc")))
   * scala> eitherT.foldF(string => string.split("").toList, _ => List("123"))
   * res0: List[String] = List(123, a, b, c)
   * }}}
   */
  def foldF[C](fa: A => F[C], fb: B => F[C])(implicit F: FlatMap[F]): F[C] = F.flatMap(value)(_.fold(fa, fb))

  /**
   * Example:
   * {{{
   * scala> import cats.implicits._
   * scala> import cats.mtl.covariant.EitherT
   *
   * scala> val eitherT: EitherT[List,String,Int] = EitherT[List, String,Int](List(Right(123),Left("abc")))
   * scala> eitherT.isLeft
   * res0: List[Boolean] = List(false, true)
   * }}}
   */
  def isLeft(implicit F: Functor[F]): F[Boolean] = F.map(value)(_.isLeft)

  /**
   * Example:
   * {{{
   * scala> import cats.implicits._
   * scala> import cats.mtl.covariant.EitherT
   *
   * scala> val eitherT: EitherT[List,String,Int] = EitherT[List, String,Int](List(Right(123),Left("abc")))
   * scala> eitherT.isRight
   * res0: List[Boolean] = List(true, false)
   * }}}
   */
  def isRight(implicit F: Functor[F]): F[Boolean] = F.map(value)(_.isRight)

  /**
   * Example:
   * {{{
   * scala> import cats.implicits._
   * scala> import cats.mtl.covariant.EitherT
   *
   * scala> val eitherT: EitherT[List,String,Int] = EitherT[List, String,Int](List(Right(123),Left("abc")))
   * scala> eitherT.swap
   * res0: EitherT[List,Int,String] = EitherT(List(Left(123), Right(abc)))
   * }}}
   */
  def swap(implicit F: Functor[F]): EitherT[F, B, A] = EitherT(F.map(value)(_.swap))

  /**
   * Example:
   * {{{
   * scala> import cats.implicits._
   * scala> import cats.mtl.covariant.EitherT
   *
   * scala> val eitherT: EitherT[List,String,Int] = EitherT[List, String,Int](List(Right(123),Left("abc")))
   * scala> eitherT.getOrElse(456)
   * res0: List[Int] = List(123, 456)
   * }}}
   */
  def getOrElse[BB >: B](default: => BB)(implicit F: Functor[F]): F[BB] = F.map(value)(_.getOrElse(default))

  /**
   * Example:
   * {{{
   * scala> import cats.implicits._
   * scala> import cats.mtl.covariant.EitherT
   *
   * scala> val eitherT: EitherT[List,String,Int] = EitherT[List, String,Int](List(Right(123),Left("abc")))
   * scala> eitherT.getOrElseF(List(456))
   * res0: List[Int] = List(123, 456)
   * }}}
   */
  def getOrElseF[BB >: B](default: => F[BB])(implicit F: Monad[F]): F[BB] =
    F.flatMap(value) {
      case Left(_)  => default
      case Right(b) => F.pure(b)
    }

  /**
   * Example:
   * {{{
   * scala> import cats.implicits._
   * scala> import cats.mtl.covariant.EitherT
   *
   * scala> val e1: EitherT[Option,String,Int] = EitherT[Option, String,Int](Some(Right(123)))
   * scala> e1.orElse(EitherT[Option,Boolean,Int](Some(Right(456))))
   * res0: EitherT[Option, Boolean, Int] = EitherT(Some(Right(123)))
   *
   * scala> val e2: EitherT[Option,String,Int] = EitherT[Option, String,Int](Some(Left("abc")))
   * scala> e2.orElse(EitherT[Option,Boolean,Int](Some(Left(true))))
   * res1: EitherT[Option, Boolean, Int] = EitherT(Some(Left(true)))
   * }}}
   */
  def orElse[C, BB >: B](default: => EitherT[F, C, BB])(implicit F: Monad[F]): EitherT[F, C, BB] =
    EitherT(F.flatMap(value) {
      case Left(_)      => default.value
      case r @ Right(_) => F.pure(r.leftCast)
    })

  /**
   * Example:
   * {{{
   * scala> import cats.implicits._
   * scala> import cats.mtl.covariant.EitherT
   *
   * scala> val eitherT: EitherT[List,String,Int] =
   *      |   EitherT[List, String,Int](List(Right(123),Left("abc")))
   * scala> val pf: PartialFunction[String, Int] = {case "abc" => 456}
   * scala> eitherT.recover(pf)
   * res0: EitherT[List, String, Int] = EitherT(List(Right(123), Right(456)))
   * }}}
   */
  def recover[BB >: B](pf: PartialFunction[A, BB])(implicit F: Functor[F]): EitherT[F, A, BB] =
    EitherT(F.map(value)(_.recover(pf)))

  /**
   * Example:
   * {{{
   * scala> import cats.implicits._
   * scala> import cats.mtl.covariant.EitherT
   *
   * scala> val eitherT: EitherT[List,String,Int] =
   *      |   EitherT[List, String,Int](List(Right(123),Left("abc")))
   * scala> val pf: PartialFunction[String, EitherT[List, String, Int]] =
   *      |   {case "abc" => EitherT[List, String, Int](List(Right(456)))}
   * scala> eitherT.recoverWith(pf)
   * res0: EitherT[List, String, Int] = EitherT(List(Right(123), Right(456)))
   * }}}
   */
  def recoverWith[AA >: A, BB >: B](pf: PartialFunction[A, EitherT[F, AA, BB]])(implicit F: Monad[F]): EitherT[F, AA, BB] =
    EitherT(F.flatMap(value) {
      case Left(a) if pf.isDefinedAt(a) => pf(a).value
      case other                        => F.pure(other)
    })

  /**
   * Inverse of `MonadError#attemptT`
   *
   * Example:
   * {{{
   * scala> import cats.implicits._
   * scala> import cats.mtl.covariant.EitherT
   *
   * scala> val e1: EitherT[Option, Unit, Int] = EitherT[Option, Unit, Int](Some(Right(123)))
   * scala> e1.rethrowT
   * res0: Option[Int] = Some(123)
   *
   * scala> val e2: EitherT[Option, Unit, Int] = EitherT[Option, Unit, Int](Some(Left(())))
   * scala> e2.rethrowT
   * res1: Option[Int] = None
   * }}}
   */
  def rethrowT(implicit F: MonadError[F, _ >: A]): F[B] =
    F.rethrow(value)

  /**
   * Example:
   * {{{
   * scala> import cats.implicits._
   * scala> import cats.mtl.covariant.EitherT
   *
   * scala> val eitherT: EitherT[List, String, Int] = EitherT[List, String, Int](List(Right(123), Left("abc")))
   * scala> eitherT.valueOr(_.length)
   * res0: List[Int] = List(123, 3)
   * }}}
   */
  def valueOr[BB >: B](f: A => BB)(implicit F: Functor[F]): F[BB] = fold(f, identity)

  /**
   * Example:
   * {{{
   * scala> import cats.implicits._
   * scala> import cats.mtl.covariant.EitherT
   *
   * scala> val eitherT: EitherT[List, String, Int] = EitherT[List, String, Int](List(Right(123), Left("abc")))
   * scala> eitherT.valueOrF(string => List(string.length))
   * res0: List[Int] = List(123, 3)
   * }}}
   */
  def valueOrF[BB >: B](f: A => F[BB])(implicit F: Monad[F]): F[BB] =
    F.flatMap(value) {
      case Left(a)  => f(a)
      case Right(b) => F.pure(b)
    }

  /**
   * Example:
   * {{{
   * scala> import cats.implicits._
   * scala> import cats.mtl.covariant.EitherT
   *
   * scala> val eitherT: EitherT[List, String, Int] = EitherT[List, String, Int](List(Right(123), Left("abc")))
   * scala> eitherT.forall(_ > 100)
   * res0: List[Boolean] = List(true, true)
   * }}}
   */
  def forall(f: B => Boolean)(implicit F: Functor[F]): F[Boolean] = F.map(value)(_.forall(f))

  /**
   * Example:
   * {{{
   * scala> import cats.implicits._
   * scala> import cats.mtl.covariant.EitherT
   *
   * scala> val eitherT: EitherT[List, String, Int] = EitherT[List, String, Int](List(Right(123), Left("abc")))
   * scala> eitherT.exists(_ > 100)
   * res0: List[Boolean] = List(true, false)
   * }}}
   */
  def exists(f: B => Boolean)(implicit F: Functor[F]): F[Boolean] = F.map(value)(_.exists(f))

  /**
   * Example:
   * {{{
   * scala> import cats.implicits._
   * scala> import cats.mtl.covariant.EitherT
   *
   * scala> val e1: EitherT[List, String, Int] = EitherT[List, String, Int](List(Right(123), Left("abc")))
   * scala> e1.ensure("error")(_ > 150)
   * res0: EitherT[List, String, Int] = EitherT(List(Left(error), Left(abc)))
   *
   * scala> val e2: EitherT[List, String, Int] = EitherT[List, String, Int](List(Right(123), Left("abc")))
   * scala> e2.ensure("error")(_ > 100)
   * res1: EitherT[List, String, Int] = EitherT(List(Right(123), Left(abc)))
   * }}}
   */
  def ensure[AA >: A](onFailure: => AA)(f: B => Boolean)(implicit F: Functor[F]): EitherT[F, AA, B] =
    EitherT(F.map(value)(_.ensure(onFailure)(f)))

  /**
   * Example:
   * {{{
   * scala> import cats.implicits._
   * scala> import cats.mtl.covariant.EitherT
   *
   * scala> val e1: EitherT[List, String, Int] = EitherT[List, String, Int](List(Right(123), Left("abc")))
   * scala> e1.ensureOr(_ => "error")(_ > 100)
   * res0: EitherT[List, String, Int] = EitherT(List(Right(123), Left(abc)))
   *
   * scala> val e2: EitherT[List, String, Int] = EitherT[List, String, Int](List(Right(123), Left("abc")))
   * scala> e2.ensureOr(_ => "error")(_ > 150)
   * res1: EitherT[List, String, Int] = EitherT(List(Left(error), Left(abc)))
   * }}}
   */
  def ensureOr[AA >: A](onFailure: B => AA)(f: B => Boolean)(implicit F: Functor[F]): EitherT[F, AA, B] =
    EitherT(F.map(value)(_.ensureOr(onFailure)(f)))

  /**
   * Example:
   * {{{
   * scala> import cats.implicits._
   * scala> import cats.mtl.covariant.EitherT
   *
   * scala> val eitherT: EitherT[List, String, Int] = EitherT[List, String, Int](List(Right(123), Left("abc")))
   * scala> eitherT.toOption
   * res0: OptionT[List, Int] = OptionT(List(Some(123), None))
   * }}}
   
  def toOption(implicit F: Functor[F]): OptionT[F, B] = OptionT(F.map(value)(_.toOption))
  */

  /**
   * Example:
   * {{{
   * scala> import cats.implicits._
   * scala> import cats.mtl.covariant.EitherT
   *
   * scala> val eitherT: EitherT[List, String, Int] = EitherT[List, String, Int](List(Right(123), Left("abc")))
   * scala> eitherT.to[Option]
   * res0: List[Option[Int]] = List(Some(123), None)
   * }}}
   */
  def to[G[_], BB >: B](implicit F: Functor[F], G: Alternative[G]): F[G[BB]] =
    F.map(value)(_.widen[BB].to[G])

  /**
   * Example:
   * {{{
   * scala> import cats.implicits._
   * scala> import cats.mtl.covariant.EitherT
   *
   * scala> val eitherT: EitherT[List, String, Int] =
   *      |   EitherT[List, String, Int](List(Right(123), Left("abc")))
   * scala> eitherT.collectRight
   * res0: List[Int] = List(123)
   * }}}
   */
  def collectRight(implicit FA: Alternative[F], FM: Monad[F]): F[B] =
    FM.flatMap(value)(_.to[F])

  /**
   * Example:
   * {{{
   * scala> import cats.implicits._
   * scala> import cats.mtl.covariant.EitherT
   *
   * scala> val eitherT: EitherT[List, String, Int] =
   *      |   EitherT[List, String, Int](List(Right(123), Left("abc")))
   * scala> eitherT.bimap(string => string.length, int => int % 100)
   * res0: EitherT[List, Int, Int] = EitherT(List(Right(23), Left(3)))
   * }}}
   */
  def bimap[C, D](fa: A => C, fb: B => D)(implicit F: Functor[F]): EitherT[F, C, D] =
    EitherT(F.map(value)(_.bimap(fa, fb)))

  def bitraverse[G[_], C, D](f: A => G[C], g: B => G[D])(implicit traverseF: Traverse[F],
                                                         applicativeG: Applicative[G]): G[EitherT[F, C, D]] =
    applicativeG.map(traverseF.traverse(value)(axb => Bitraverse[Either].bitraverse(axb)(f, g)))(EitherT.apply)

  def biflatMap[C, D](fa: A => EitherT[F, C, D], fb: B => EitherT[F, C, D])(implicit F: FlatMap[F]): EitherT[F, C, D] =
    EitherT(F.flatMap(value) {
      case Left(a)  => fa(a).value
      case Right(a) => fb(a).value
    })

  def applyAlt[AA >: A, D](ff: EitherT[F, AA, B => D])(implicit F: Apply[F]): EitherT[F, AA, D] =
    EitherT[F, AA, D](F.map2(this.value, ff.value)((xb, xbd) => Apply[Either[AA, *]].ap(xbd)(xb)))

  def flatMap[AA >: A, D](f: B => EitherT[F, AA, D])(implicit F: Monad[F]): EitherT[F, AA, D] =
    EitherT(F.flatMap(value) {
      case l @ Left(_) => F.pure(l.rightCast)
      case Right(b)    => f(b).value
    })

  def flatMapF[AA >: A, D](f: B => F[Either[AA, D]])(implicit F: Monad[F]): EitherT[F, AA, D] =
    flatMap(b => EitherT(f(b)))

  def transform[C, D](f: Either[A, B] => Either[C, D])(implicit F: Functor[F]): EitherT[F, C, D] =
    EitherT(F.map(value)(f))

  def subflatMap[AA >: A, D](f: B => Either[AA, D])(implicit F: Functor[F]): EitherT[F, AA, D] =
    transform(_.flatMap(f))

  def map[D](f: B => D)(implicit F: Functor[F]): EitherT[F, A, D] = bimap(identity, f)

  /**
   * Modify the context `F` using transformation `f`.
   */
  def mapK[G[+_]](f: F ~> G): EitherT[G, A, B] = EitherT[G, A, B](f(value))

  def semiflatMap[D](f: B => F[D])(implicit F: Monad[F]): EitherT[F, A, D] =
    flatMap(b => EitherT.right(f(b)))

  def leftMap[C](f: A => C)(implicit F: Functor[F]): EitherT[F, C, B] = bimap(f, identity)

  def leftFlatMap[BB >: B, C](f: A => EitherT[F, C, BB])(implicit F: Monad[F]): EitherT[F, C, BB] =
    EitherT(F.flatMap(value) {
      case Left(a)      => f(a).value
      case r @ Right(_) => F.pure(r.leftCast)
    })

  def leftSemiflatMap[D](f: A => F[D])(implicit F: Monad[F]): EitherT[F, D, B] =
    EitherT(F.flatMap(value) {
      case Left(a) =>
        F.map(f(a)) { d =>
          Left(d)
        }
      case r @ Right(_) => F.pure(r.leftCast)
    })

  /** Combine `leftSemiflatMap` and `semiflatMap` together.
   *
   * Example:
   * {{{
   * scala> import cats.implicits._
   * scala> import cats.mtl.covariant.EitherT
   *
   * scala> val eitherT: EitherT[List, String, Int] = EitherT[List, String, Int](List(Left("abc"), Right(123)))
   * scala> eitherT.biSemiflatMap(string => List(string.length), int => List(int.toFloat))
   * res0: cats.mtl.covariant.EitherT[List,Int,Float] = EitherT(List(Left(3), Right(123.0)))
   * }}}
   */
  def biSemiflatMap[C, D](fa: A => F[C], fb: B => F[D])(implicit F: Monad[F]): EitherT[F, C, D] =
    EitherT(F.flatMap(value) {
      case Left(a) =>
        F.map(fa(a)) { c =>
          Left(c)
        }
      case Right(b) =>
        F.map(fb(b)) { d =>
          Right(d)
        }
    })

  def compare[AA >: A, BB >: B](that: EitherT[F, AA, BB])(implicit o: Order[F[Either[AA, BB]]]): Int =
    o.compare(value, that.value)

  def partialCompare[AA >: A, BB >: B](that: EitherT[F, AA, BB])(implicit p: PartialOrder[F[Either[AA, BB]]]): Double =
    p.partialCompare(value, that.value)

  def ===[AA >: A, BB >: B](that: EitherT[F, AA, BB])(implicit eq: Eq[F[Either[AA, BB]]]): Boolean =
    eq.eqv(value, that.value)

  def traverse[G[_], AA >: A, D](f: B => G[D])(implicit traverseF: Traverse[F],
                                      applicativeG: Applicative[G]): G[EitherT[F, AA, D]] =
    applicativeG.map(traverseF.traverse(value)(axb => Traverse[Either[AA, *]].traverse(axb)(f)))(EitherT.apply)

  def foldLeft[C](c: C)(f: (C, B) => C)(implicit F: Foldable[F]): C =
    F.foldLeft(value, c)((c, axb) => axb.foldLeft(c)(f))

  def foldRight[C](lc: Eval[C])(f: (B, Eval[C]) => Eval[C])(implicit F: Foldable[F]): Eval[C] =
    F.foldRight(value, lc)((axb, lc) => axb.foldRight(lc)(f))

  def merge[AA >: A](implicit ev: B <:< AA, F: Functor[F]): F[AA] = F.map(value)(_.fold(identity, ev.apply))

  /**
   * Similar to `Either#combine` but mapped over an `F` context.
   *
   * Examples:
   * {{{
   * scala> import cats.mtl.covariant.EitherT
   * scala> import cats.implicits._
   * scala> val l1: EitherT[Option, String, Int] = EitherT.left(Some("error 1"))
   * scala> val l2: EitherT[Option, String, Int] = EitherT.left(Some("error 2"))
   * scala> val r3: EitherT[Option, String, Int] = EitherT.right(Some(3))
   * scala> val r4: EitherT[Option, String, Int] = EitherT.right(Some(4))
   * scala> val noneEitherT: EitherT[Option, String, Int] = EitherT.left(None)
   *
   * scala> l1 combine l2
   * res0: EitherT[Option, String, Int] = EitherT(Some(Left(error 1)))
   *
   * scala> l1 combine r3
   * res1: EitherT[Option, String, Int] = EitherT(Some(Left(error 1)))
   *
   * scala> r3 combine l1
   * res2: EitherT[Option, String, Int] = EitherT(Some(Left(error 1)))
   *
   * scala> r3 combine r4
   * res3: EitherT[Option, String, Int] = EitherT(Some(Right(7)))
   *
   * scala> l1 combine noneEitherT
   * res4: EitherT[Option, String, Int] = EitherT(None)
   *
   * scala> noneEitherT combine l1
   * res5: EitherT[Option, String, Int] = EitherT(None)
   *
   * scala> r3 combine noneEitherT
   * res6: EitherT[Option, String, Int] = EitherT(None)
   *
   * scala> noneEitherT combine r4
   * res7: EitherT[Option, String, Int] = EitherT(None)
   * }}}
   */
  def combine[AA >: A, BB >: B](that: EitherT[F, AA, BB])(implicit F: Apply[F], B: Semigroup[BB]): EitherT[F, AA, BB] =
    EitherT(F.map2(this.value, that.value)(_.combine(_)))

  def toValidated(implicit F: Functor[F]): F[Validated[A, B]] =
    F.map(value)(_.toValidated)

  def toValidatedNel(implicit F: Functor[F]): F[ValidatedNel[A, B]] =
    F.map(value)(_.toValidatedNel)

  def toValidatedNec(implicit F: Functor[F]): F[ValidatedNec[A, B]] =
    F.map(value)(_.toValidatedNec)

  /** Run this value as a `[[Validated]]` against the function and convert it back to an `[[EitherT]]`.
   *
   * The [[Applicative]] instance for `EitherT` "fails fast" - it is often useful to "momentarily" have
   * it accumulate errors instead, which is what the `[[Validated]]` data type gives us.
   *
   * Example:
   * {{{
   * scala> import cats.implicits._
   * scala> type Error = String
   * scala> val v1: Validated[NonEmptyList[Error], Int] = Validated.invalidNel("error 1")
   * scala> val v2: Validated[NonEmptyList[Error], Int] = Validated.invalidNel("error 2")
   * scala> val eithert: EitherT[Option, Error, Int] = EitherT.leftT[Option, Int]("error 3")
   * scala> eithert.withValidated { v3 => (v1, v2, v3.toValidatedNel).mapN { case (i, j, k) => i + j + k } }
   * res0: EitherT[Option, NonEmptyList[Error], Int] = EitherT(Some(Left(NonEmptyList(error 1, error 2, error 3))))
   * }}}
   */
  def withValidated[C, D](f: Validated[A, B] => Validated[C, D])(implicit F: Functor[F]): EitherT[F, C, D] =
    EitherT(F.map(value)(either => f(either.toValidated).toEither))

  def show[AA >: A, BB >: B](implicit show: Show[F[Either[AA, BB]]]): String = show.show(value)

  /**
   * Transform this `EitherT[F, A, B]` into a `[[Nested]][F, Either[A, *], B]`.
   *
   * An example where `toNested` can be used, is to get the `Apply.ap` function with the
   * behavior from the composed `Apply` instances from `F` and `Either[A, *]`, which is
   * inconsistent with the behavior of the `ap` from `Monad` of `EitherT`.
   *
   * {{{
   * scala> import cats.mtl.covariant.EitherT
   * scala> import cats.implicits._
   * scala> val ff: EitherT[List, String, Int => String] =
   *      |   EitherT(List(Either.right(_.toString), Either.left("error")))
   * scala> val fa: EitherT[List, String, Int] =
   *      |   EitherT(List(Either.right(1), Either.right(2)))
   * scala> ff.ap(fa)
   * res0: EitherT[List,String,String] = EitherT(List(Right(1), Right(2), Left(error)))
   * scala> EitherT((ff.toNested).ap(fa.toNested).value)
   * res1: EitherT[List,String,String] = EitherT(List(Right(1), Right(2), Left(error), Left(error)))
   * }}}
   *
   */
  def toNested[AA >: A, BB >: B]: Nested[F, Either[AA, *], BB] = Nested[F, Either[AA, *], BB](value)

  /**
   * Transform this `EitherT[F, A, B]` into a `[[Nested]][F, Validated[A, *], B]`.
   *
   * Example:
   * {{{
   * scala> import cats.mtl.covariant.{EitherT, Validated}
   * scala> import cats.implicits._
   * scala> val f: Int => String = i => (i*2).toString
   * scala> val r1: EitherT[Option, String, Int => String] = EitherT.right(Some(f))
   * r1: cats.mtl.covariant.EitherT[Option,String,Int => String] = EitherT(Some(Right(<function1>)))
   * scala> val r2: EitherT[Option, String, Int] = EitherT.right(Some(10))
   * r2: cats.mtl.covariant.EitherT[Option,String,Int] = EitherT(Some(Right(10)))
   * scala> type ErrorOr[A] = Validated[String, A]
   * scala> (r1.toNestedValidated).ap(r2.toNestedValidated)
   * res0: cats.data.Nested[Option,ErrorOr,String] = Nested(Some(Valid(20)))
   * }}}
   */
  def toNestedValidated[AA >: A, BB >: B](implicit F: Functor[F]): Nested[F, Validated[AA, *], BB] =
    Nested[F, Validated[AA, *], BB](F.map(value)(_.toValidated))

  /**
   * Transform this `EitherT[F, A, B]` into a `[[Nested]][F, ValidatedNel[A, *], B]`.
   */
  def toNestedValidatedNel[AA >: A, BB >: B](implicit F: Functor[F]): Nested[F, ValidatedNel[AA, *], BB] =
    Nested[F, ValidatedNel[AA, *], BB](F.map(value)(_.toValidatedNel))

  /**
   * Transform this `EitherT[F, A, B]` into a `[[Nested]][F, ValidatedNec[A, *], B]`.
   */
  def toNestedValidatedNec[AA >: A, BB >: B](implicit F: Functor[F]): Nested[F, ValidatedNec[AA, *], BB] =
    Nested[F, ValidatedNec[AA, *], BB](F.map(value)(_.toValidatedNec))
}

object EitherT {
  def apply[F[+_], A, B](value: F[Either[A, B]]): EitherT[F, A, B] =
    new EitherT[F, A, B](value)

  /**
   * Uses the [[http://typelevel.org/cats/guidelines.html#partially-applied-type-params Partially Applied Type Params technique]] for ergonomics.
   */
  final private[covariant] class LeftPartiallyApplied[+B](private val dummy: Boolean = true) extends AnyVal {
    def apply[F[+_], A](fa: F[A])(implicit F: Functor[F]): EitherT[F, A, B] = EitherT(F.map(fa)(Either.left))
  }

  /**
   * Creates a left version of `EitherT[F, A, B]` from a `F[A]`
   * {{{
   * scala> import cats.mtl.covariant.EitherT
   * scala> import cats.implicits._
   * scala> EitherT.left[Int](Option("err"))
   * res0: cats.mtl.covariant.EitherT[Option,String,Int] = EitherT(Some(Left(err)))
   * }}}
   */
  final def left[B]: LeftPartiallyApplied[B] = new LeftPartiallyApplied[B]

  /**
   * Uses the [[http://typelevel.org/cats/guidelines.html#partially-applied-type-params Partially Applied Type Params technique]] for ergonomics.
   */
  final private[covariant] class LeftTPartiallyApplied[F[+_], +B](private val dummy: Boolean = true) extends AnyVal {
    def apply[A](a: A)(implicit F: Applicative[F]): EitherT[F, A, B] = EitherT(F.pure(Either.left(a)))
  }

  /**
   * Creates a left version of `EitherT[F, A, B]` from a `A`
   * {{{
   * scala> import cats.mtl.covariant.EitherT
   * scala> import cats.implicits._
   * scala> EitherT.leftT[Option, Int]("err")
   * res0: cats.mtl.covariant.EitherT[Option,String,Int] = EitherT(Some(Left(err)))
   * }}}
   */
  final def leftT[F[+_], B]: LeftTPartiallyApplied[F, B] = new LeftTPartiallyApplied[F, B]

  /**
   * Uses the [[http://typelevel.org/cats/guidelines.html#partially-applied-type-params Partially Applied Type Params technique]] for ergonomics.
   */
  final private[covariant] class RightPartiallyApplied[+A](private val dummy: Boolean = true) extends AnyVal {
    def apply[F[+_], B](fb: F[B])(implicit F: Functor[F]): EitherT[F, A, B] = EitherT(F.map(fb)(Either.right))
  }

  /**
   * Creates a right version of `EitherT[F, A, B]` from a `F[B]`
   * {{{
   * scala> import cats.mtl.covariant.EitherT
   * scala> import cats.implicits._
   * scala> EitherT.right[String](Option(3))
   * res0: cats.mtl.covariant.EitherT[Option,String,Int] = EitherT(Some(Right(3)))
   * }}}
   */
  final def right[A]: RightPartiallyApplied[A] = new RightPartiallyApplied[A]

  /**
   * Uses the [[http://typelevel.org/cats/guidelines.html#partially-applied-type-params Partially Applied Type Params technique]] for ergonomics.
   */
  final private[covariant] class PurePartiallyApplied[F[+_], +A](private val dummy: Boolean = true) extends AnyVal {
    def apply[B](b: B)(implicit F: Applicative[F]): EitherT[F, A, B] = right(F.pure(b))
  }

  /**
   * Creates a new `EitherT[F, A, B]` from a `B`
   * {{{
   * scala> import cats.mtl.covariant.EitherT
   * scala> import cats.implicits._
   * scala> EitherT.pure[Option, String](3)
   * res0: cats.mtl.covariant.EitherT[Option,String,Int] = EitherT(Some(Right(3)))
   * }}}
   */
  final def pure[F[+_], A]: PurePartiallyApplied[F, A] = new PurePartiallyApplied[F, A]

  /**
   * Alias for [[pure]]
   * {{{
   * scala> import cats.mtl.covariant.EitherT
   * scala> import cats.implicits._
   * scala> EitherT.rightT[Option, String](3)
   * res0: cats.mtl.covariant.EitherT[Option,String,Int] = EitherT(Some(Right(3)))
   * }}}
   */
  final def rightT[F[+_], A]: PurePartiallyApplied[F, A] = pure

  /**
   * Alias for [[right]]
   * {{{
   * scala> import cats.mtl.covariant.EitherT
   * scala> import cats.implicits._
   * scala> val o: Option[Int] = Some(3)
   * scala> val n: Option[Int] = None
   * scala> EitherT.liftF(o)
   * res0: cats.mtl.covariant.EitherT[Option,Nothing,Int] = EitherT(Some(Right(3)))
   * scala> EitherT.liftF(n)
   * res1: cats.mtl.covariant.EitherT[Option,Nothing,Int] = EitherT(None)
   * }}}
   */
  final def liftF[F[+_], A, B](fb: F[B])(implicit F: Functor[F]): EitherT[F, A, B] = right(fb)

  /**
   * Same as [[liftF]], but expressed as a FunctionK for use with mapK
   * {{{
   * scala> import cats._, mtl.covariant._, implicits._
   * scala> val a: OptionT[Eval, Int] = 1.pure[OptionT[Eval, *]]
   * scala> val b: OptionT[EitherT[Eval, String, *], Int] = a.mapK(EitherT.liftK)
   * scala> b.value.value.value
   * res0: Either[String,Option[Int]] = Right(Some(1))
   * }}}
   */
  final def liftK[F[+_], A](implicit F: Functor[F]): F ~> EitherT[F, A, *] = new (F ~> EitherT[F, A, *]) {
    def apply[B](fa: F[B]): EitherT[F,A,B] = liftF(fa)
  }


  /** Transforms an `Either` into an `EitherT`, lifted into the specified `Applicative`.
   *
   * Note: The return type is a FromEitherPartiallyApplied[F], which has an apply method
   * on it, allowing you to call fromEither like this:
   * {{{
   * scala> import cats.implicits._
   * scala> val t: Either[String, Int] = Either.right(3)
   * scala> EitherT.fromEither[Option](t)
   * res0: EitherT[Option, String, Int] = EitherT(Some(Right(3)))
   * }}}
   *
   * The reason for the indirection is to emulate currying type parameters.
   */
  final def fromEither[F[+_]]: FromEitherPartiallyApplied[F] = new FromEitherPartiallyApplied

  /**
   * Uses the [[http://typelevel.org/cats/guidelines.html#partially-applied-type-params Partially Applied Type Params technique]] for ergonomics.
   */
  final private[covariant] class FromEitherPartiallyApplied[F[+_]](private val dummy: Boolean = true) extends AnyVal {
    def apply[E, A](either: Either[E, A])(implicit F: Applicative[F]): EitherT[F, E, A] =
      EitherT(F.pure(either))
  }

  /** Transforms an `Option` into an `EitherT`, lifted into the specified `Applicative` and using
   *  the second argument if the `Option` is a `None`.
   * {{{
   * scala> import cats.implicits._
   * scala> val o: Option[Int] = None
   * scala> EitherT.fromOption[List](o, "Answer not known.")
   * res0: EitherT[List, String, Int]  = EitherT(List(Left(Answer not known.)))
   * scala> EitherT.fromOption[List](Some(42), "Answer not known.")
   * res1: EitherT[List, String, Int] = EitherT(List(Right(42)))
   * }}}
   */
  final def fromOption[F[+_]]: FromOptionPartiallyApplied[F] = new FromOptionPartiallyApplied

  /**
   * Uses the [[http://typelevel.org/cats/guidelines.html#partially-applied-type-params Partially Applied Type Params technique]] for ergonomics.
   */
  final private[covariant] class FromOptionPartiallyApplied[F[+_]](private val dummy: Boolean = true) extends AnyVal {
    def apply[E, A](opt: Option[A], ifNone: => E)(implicit F: Applicative[F]): EitherT[F, E, A] =
      EitherT(F.pure(Either.fromOption(opt, ifNone)))
  }

  /** Transforms an `F[Option]` into an `EitherT`, using the second argument if the `Option` is a `None`.
   * {{{
   * scala> import cats.implicits._
   * scala> val o: Option[Int] = None
   * scala> EitherT.fromOptionF(List(o), "Answer not known.")
   * res0: EitherT[List, String, Int]  = EitherT(List(Left(Answer not known.)))
   * scala> EitherT.fromOptionF(List(Option(42)), "Answer not known.")
   * res1: EitherT[List, String, Int] = EitherT(List(Right(42)))
   * }}}
   */
  final def fromOptionF[F[+_], E, A](fopt: F[Option[A]], ifNone: => E)(implicit F: Functor[F]): EitherT[F, E, A] =
    EitherT(F.map(fopt)(opt => Either.fromOption(opt, ifNone)))

  /**  If the condition is satisfied, return the given `A` in `Right`
   *  lifted into the specified `Applicative`, otherwise, return the
   *  given `E` in `Left` lifted into the specified `Applicative`.
   *
   * {{{
   * scala> import cats.Id
   * scala> import cats.mtl.covariant.EitherT
   * scala> val userInput = "hello world"
   * scala> EitherT.cond[Id](
   *      |   userInput.forall(_.isDigit) && userInput.size == 10,
   *      |   userInput,
   *      |   "The input does not look like a phone number")
   * res0: EitherT[Id, String, String] = EitherT(Left(The input does not look like a phone number))
   * }}}
   */
  final def cond[F[+_]]: CondPartiallyApplied[F] = new CondPartiallyApplied

  /**
   * Uses the [[http://typelevel.org/cats/guidelines.html#partially-applied-type-params Partially Applied Type Params technique]] for ergonomics.
   */
  final private[covariant] class CondPartiallyApplied[F[+_]](private val dummy: Boolean = true) extends AnyVal {
    def apply[E, A](test: Boolean, right: => A, left: => E)(implicit F: Applicative[F]): EitherT[F, E, A] =
      EitherT(F.pure(Either.cond(test, right, left)))
  }
}
