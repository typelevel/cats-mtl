package cats
package mtl
package syntax

trait AskingSyntax {
  implicit def toReaderOps[E, A](fun: E => A): ReaderOps[E, A]  = new ReaderOps(fun)
}

final class ReaderOps[E, A](val fun: E => A) extends AnyVal {
  def reader[F[_]](implicit asking: applicative.Asking[F, E]): F[A] =
    asking.reader(fun)
}

object asking extends AskingSyntax

