package cats
package mtl
package syntax

trait TellSyntax {
  implicit def toTellOps[L](e: L): TellOps[L] = new TellOps(e)
}

final class TellOps[L](val e: L) extends AnyVal {
  def tell[F[_]](implicit applicativeTell: ApplicativeTell[F, L]): F[Unit] = applicativeTell.tell(e)
}

object tell extends TellSyntax
