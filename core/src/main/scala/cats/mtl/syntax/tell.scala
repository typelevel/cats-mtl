package cats
package mtl
package syntax

trait TellSyntax {
  implicit def toTellOps[L](e: L): TellOps[L] = new TellOps(e)
  implicit def toTupleOps[L, A](t: (L, A)): TupleOps[L, A] = new TupleOps(t)
}

final class TupleOps[L, A](val t: (L, A)) extends AnyVal {
  def tuple[F[_]](implicit functorTell: FunctorTell[F, L]): F[A] = functorTell.tuple(t)
}

final class TellOps[L](val e: L) extends AnyVal {
  def tell[F[_]](implicit functorTell: FunctorTell[F, L]): F[Unit] = functorTell.tell(e)
}

object tell extends TellSyntax
