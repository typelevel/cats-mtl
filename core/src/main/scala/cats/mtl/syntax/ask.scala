package cats
package mtl
package syntax

trait AskSyntax {


}

final class ReaderOps[E, A](val fun: E => A) extends AnyVal {
  def reader[F[_]](implicit askN: monad.Asking[F, E]): F[A] =
    askN.reader(fun)
}

object ask {

}
