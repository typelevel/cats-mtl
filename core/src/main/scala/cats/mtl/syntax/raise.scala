package cats
package mtl
package syntax

trait RaiseSyntax {
  implicit def toRaiseOps[E](e: E): RaiseOps[E] = new RaiseOps(e)
}

final class RaiseOps[E](val e: E) extends AnyVal {
  def raise[F[_], A](implicit functorRaise: FunctorRaise[F, E]): F[A] = functorRaise.raise(e)
}

object raise extends RaiseSyntax
