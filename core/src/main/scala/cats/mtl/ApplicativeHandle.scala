package cats
package mtl

import cats.data.EitherT

import scala.util.{Failure, Success, Try}
import scala.util.control.NonFatal

/**
  * `ApplicativeHandle` has one external law:
  * {{{
  * def materializeRecoversRaise(e: E) = {
  *   attempt(raise(e)) <-> pure(Left(e))
  * }
  * }}}
  *
  * `ApplicativeHandle` has one internal law:
  * {{{
  * def handleErrorWithActsOnRaisedError[A](e: E, f: E => F[A]): IsEq[F[A]] = {
  *   F.handleErrorWith(F.raiseError[A](e))(f) <-> f(e)
  * }
  *
  * def handleErrorActsOnRaisedError[A](e: E, f: E => A): IsEq[F[A]] = {
  *   F.handleError(F.raiseError[A](e))(f) <-> F.pure(f(e))
  * }
  *
  * def handleErrorWithRespectsPure[A](a: A, f: E => F[A]): IsEq[F[A]] = {
  *   F.handleErrorWith(F.pure(a))(f) <-> F.pure(a)
  * }
  *
  * def handleErrorRespectsPure[A](a: A, f: E => A): IsEq[F[A]] = {
  *   F.handleError(F.pure(a))(f) <-> F.pure(a)
  * }
  *
  * def attemptActsOnRaisedError(e: E): IsEq[F[Either[E, Unit]]] = {
  *   F.attempt(F.raiseError[Unit](e)) <-> F.pure(Left(e))
  * }
  *
  * def pureAttempt[A](a: A): IsEq[F[Either[E, A]]] = {
  *   F.attempt(F.pure(a)) <-> F.pure(Right(a))
  * }
  *
  * def handleErrorWithConsistentWithRecoverWith[A](fa: F[A], f: E => F[A]): IsEq[F[A]] = {
  *   F.handleErrorWith(fa)(f) <-> F.recoverWith(fa)(PartialFunction(f))
  * }
  *
  * def handleErrorConsistentWithRecover[A](fa: F[A], f: E => A): IsEq[F[A]] = {
  *   F.handleError(fa)(f) <-> F.recover(fa)(PartialFunction(f))
  * }
  *
  * def recoverConsistentWithRecoverWith[A](fa: F[A], pf: PartialFunction[E, A]): IsEq[F[A]] = {
  *   F.recover(fa)(pf) <-> F.recoverWith(fa)(pf andThen F.pure)
  * }
  *
  * def attemptConsistentWithAttemptT[A](fa: F[A]): IsEq[EitherT[F, E, A]] = {
  *   EitherT(F.attempt(fa)) <-> F.attemptT(fa)
  * }
  * }}}
  */
trait ApplicativeHandle[F[_], E] extends Serializable {
  val applicative: Applicative[F]

  val raise: FunctorRaise[F, E]

  def attempt[A](fa: F[A]): F[E Either A]

  final def attemptT[A](fa: F[A]): EitherT[F, E, A] = EitherT(attempt(fa))

  def recover[A](fa: F[A])(f: PartialFunction[E, A]): F[A]

  def recoverWith[A](fa: F[A])(f: PartialFunction[E, F[A]]): F[A]

  /**
    * Handle any error, potentially recovering from it, by mapping it to an
    * `F[A]` value.
    *
    * @see [[handleError]] to handle any error by simply mapping it to an `A`
    * value instead of an `F[A]`.
    *
    * @see [[recoverWith]] to recover from only certain errors.
    */
  def handleErrorWith[A](fa: F[A])(f: E => F[A]): F[A]

  /**
    * Handle any error, by mapping it to an `A` value.
    *
    * @see [[handleErrorWith]] to map to an `F[A]` value instead of simply an
    * `A` value.
    *
    * @see [[recover]] to only recover from certain errors.
    */
  def handleError[A](fa: F[A])(f: E => A): F[A]

  /**
    * Often E is Throwable. Here we try to call pure or catch
    * and raise.
    */
  def catchNonFatal[A](a: => A)(implicit ev: Throwable <:< E): F[A] =
    try applicative.pure(a)
    catch {
      case NonFatal(e) => raise.raise(e)
    }

  /**
    * Often E is Throwable. Here we try to call pure or catch
    * and raise
    */
  def catchNonFatalEval[A](a: Eval[A])(implicit ev: Throwable <:< E): F[A] =
    try applicative.pure(a.value)
    catch {
      case NonFatal(e) => raise.raise(e)
    }

  /**
    * If the error type is Throwable, we can convert from a scala.util.Try
    */
  def fromTry[A](t: Try[A])(implicit ev: Throwable <:< E): F[A] =
    t match {
      case Success(a) => applicative.pure(a)
      case Failure(e) => raise.raise(e)
    }
}
