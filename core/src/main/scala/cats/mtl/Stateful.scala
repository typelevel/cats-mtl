package cats
package mtl

trait Stateful[F[_], S] {
  def get: F[S]

  def set(s: S): F[Unit]

  def modify(f: S => S): F[Unit]
}

