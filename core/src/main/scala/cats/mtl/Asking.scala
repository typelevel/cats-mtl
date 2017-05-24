package cats
package mtl

trait Asking[F[_], E] {
  def ask: F[E]

  def reader[A](f: E => A): F[A]
}

object Asking {

  def ask[F[_], E](implicit ask: Asking[F, E]): F[E] =
    ask.ask

  def askE[E] = new askEPartiallyApplied[E]

  def askF[F[_]] = new askFPartiallyApplied[F]

  final private[mtl] class askEPartiallyApplied[E](val dummy: Boolean = false) extends AnyVal {
    @inline def apply[F[_]]()(implicit ask: Asking[F, E]): F[E] =
      ask.ask
  }

  final private[mtl] class askFPartiallyApplied[F[_]](val dummy: Boolean = false) extends AnyVal {
    @inline def apply[E]()(implicit ask: Asking[F, E]): F[E] =
      ask.ask
  }

  final private[mtl] class readerFEPartiallyApplied[F[_], E](val dummy: Boolean = false) extends AnyVal {
    @inline def apply[A](f: E => A)(implicit ask: Asking[F, E]): F[A] =
      ask.reader(f)
  }

  def readerFE[F[_], E] = new readerFEPartiallyApplied[F, E]

  def reader[F[_], E, A](fun: E => A)(implicit ask: Asking[F, E]): F[A] =
    ask.reader(fun)

}

