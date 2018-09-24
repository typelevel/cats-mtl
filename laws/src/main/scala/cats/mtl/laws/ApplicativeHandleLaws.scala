package cats
package mtl
package laws

import cats.laws.IsEq
import cats.laws.IsEqArrow

import scala.util.control.NonFatal

trait ApplicativeHandleLaws[F[_], E] extends FunctorRaiseLaws[F, E] {
  implicit val handleInstance: ApplicativeHandle[F, E]
  implicit val applicativeInstance: Applicative[F] = handleInstance.applicative

  import handleInstance.{handle, handleWith, attempt}
  import raiseInstance.{raise, catchNonFatal}
  import handleInstance.applicative._

  // external laws:
  def raiseAndHandleWithIsFunctionApplication[A](e: E, f: E => F[A]): IsEq[F[A]] =
    handleWith(raise[A](e))(f) <-> f(e)

  def raiseAndHandleIsPure[A](e: E, f: E => A): IsEq[F[A]] =
    handle(raise[A](e))(f) <-> pure(f(e))

  def handleWithPureIsPure[A](a: A, f: E => F[A]): IsEq[F[A]] =
    handleWith(pure(a))(f) <-> pure(a)

  def handlePureIsPure[A](a: A, f: E => A): IsEq[F[A]] =
    handle(pure(a))(f) <-> pure(a)

  def raiseAttemptIsPureLeft(e: E): IsEq[F[Either[E, Unit]]] =
    attempt(raise[Unit](e)) <-> pure(Left(e))

  def pureAttemptIsPureRight[A](a: A): IsEq[F[Either[E, A]]] =
    attempt(pure(a)) <-> pure(Right(a))

  // internal laws:
  def catchNonFatalDefault[A](a: A, f: Throwable => E): IsEq[F[A]] =
    catchNonFatal(a)(f) <-> (try {
      pure(a)
    } catch {
      case NonFatal(ex) => raise(f(ex))
    })

}

object ApplicativeHandleLaws {
  def apply[F[_], E](implicit instance0: ApplicativeHandle[F, E]): ApplicativeHandleLaws[F, E] = {
    new ApplicativeHandleLaws[F, E] {
      lazy val handleInstance: ApplicativeHandle[F, E] = instance0
      override lazy val raiseInstance: FunctorRaise[F, E] = instance0
    }
  }
}
