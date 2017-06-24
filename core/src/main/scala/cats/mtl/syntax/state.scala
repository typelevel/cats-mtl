package cats
package mtl
package syntax

trait StateSyntax {
  implicit def toSetOps[S](e: S): SetOps[S] = new SetOps(e)
  implicit def toModifyOps[S](f: S => S): ModifyOps[S] = new ModifyOps(f)
}

final class SetOps[S](val s: S) extends AnyVal {
  def set[F[_]](implicit monadState: MonadState[F, S]): F[Unit] = monadState.set(s)
}

final class ModifyOps[S](val f: S => S) extends AnyVal {
  def modify[F[_]](implicit monadState: MonadState[F, S]): F[Unit] = monadState.modify(f)
}

object state extends StateSyntax
