package cats
package mtl
package laws

import cats.laws.IsEq
import cats.laws.IsEqArrow
import cats.syntax.flatMap._
import cats.syntax.functor._

import scala.util.control.NonFatal

trait FunctorRaiseLaws[F[_], E] {
  implicit val raiseInstance: FunctorRaise[F, E]
  implicit val functor: Functor[F] = raiseInstance.functor

  import raiseInstance._

  // internal laws:
  def catchNonFatalDefault[A](a: A, f: Throwable => E)(implicit A: Applicative[F]): IsEq[F[A]] =
    catchNonFatal(a)(f) <-> (try {
      A.pure(a)
    } catch {
      case NonFatal(ex) => raise(f(ex))
    })

  def ensureDefault[A](fa: F[A], error: E, predicate: A => Boolean)(implicit A: Monad[F]): IsEq[F[A]] =
    ensure(fa)(error)(predicate) <-> (for {
      a <- fa
      _ <- if (predicate(a)) A.pure(()) else raise(error)
    } yield a)

}

object FunctorRaiseLaws {
  def apply[F[_], E](implicit instance0: FunctorRaise[F, E]): FunctorRaiseLaws[F, E] = {
    new FunctorRaiseLaws[F, E] {
      override lazy val raiseInstance: FunctorRaise[F, E] = instance0
    }
  }
}
