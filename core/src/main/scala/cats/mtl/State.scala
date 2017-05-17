package cats
package mtl

trait State[F[_], S] {
  val monad: Monad[F]

  def get: F[S]

  def set(s: S): F[Unit]

  def modify(f: S => S): F[Unit]

}

