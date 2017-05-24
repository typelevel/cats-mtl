package cats
package mtl

trait Bracketing[F[_]] {
  def bracket[A, B, C](action: F[A])(bind: A => F[B],
                                     cleanup: A => F[C]): F[B]
}
