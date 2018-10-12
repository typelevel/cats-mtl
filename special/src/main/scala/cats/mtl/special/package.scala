package cats.mtl

package object special {
  type StateIO[S, A] = StateIO.Type[S, A]
}
