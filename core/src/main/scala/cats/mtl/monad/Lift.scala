package cats
package mtl
package monad

trait Lift[B[_], M[_]] {
  def lift[A](ba: B[A]): M[A]
}

object Lift {
  implicit def liftId[M[_]]: Lift[M, M] = new Lift[M, M] {
    def lift[A](ba: M[A]): M[A] = ba
  }
  implicit def liftLayer[M[_], Inner[_]](implicit layer: monad.Layer[M, Inner]): Lift[Inner, M] = new Lift[Inner, M] {
    def lift[A](ba: Inner[A]): M[A] = layer.layer(ba)
  }
  implicit def liftComposeInd[M[_], B[_], B2[_]]
  (implicit liftF1: Lift[B, M], liftF2: Lift[B2, B]): Lift[B2, M] = new Lift[B2, M] {
    def lift[A](ba: B2[A]): M[A] = liftF1.lift(liftF2.lift(ba))
  }
}
