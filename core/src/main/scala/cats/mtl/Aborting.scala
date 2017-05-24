package cats
package mtl

trait Aborting[F[_]] extends Handling[F, Unit] {
  def abort[A]: F[A] = raise.raiseError(())
}
