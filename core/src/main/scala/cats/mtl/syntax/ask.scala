package cats
package mtl
package syntax

import cats.mtl.applicative.Asking

trait AskSyntax {


}

final class ReaderOps[E, A](val fun: E => A) extends AnyVal {
  def reader[F[_]](implicit askN: Asking[F, E]): F[A] =
    askN.reader(fun)
}

object ask {

}
