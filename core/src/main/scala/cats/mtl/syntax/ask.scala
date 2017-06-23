package cats
package mtl
package syntax

trait AskSyntax {
  implicit def toReaderOps[E, A](fun: E => A): ReaderOps[E, A]  = new ReaderOps(fun)
}

final class ReaderOps[E, A](val fun: E => A) extends AnyVal {
  def reader[F[_]](implicit ask: ApplicativeAsk[F, E]): F[A] =
    ask.reader(fun)
}

object ask extends AskSyntax

