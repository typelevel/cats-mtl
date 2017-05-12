package cats.mtl.syntax

import cats.mtl.Ask

trait AskSyntax {


}

final class ReaderOps[E, A](val fun: E => A) extends AnyVal {
  def reader[F[_]](implicit askN: Ask[F, E]): F[A] =
    askN.reader(fun)
}

object ask {

}
