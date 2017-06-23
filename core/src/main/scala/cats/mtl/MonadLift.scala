package cats
package mtl

trait MonadLift[B[_], M[_]] {
  def lift[A](ba: B[A]): M[A]
}

object MonadLift {

  implicit def liftId[M[_]]: MonadLift[M, M] = {
    new MonadLift[M, M] {
      def lift[A](ba: M[A]): M[A] = ba
    }
  }

  implicit def liftLayer[M[_], Inner[_]](implicit layer: MonadLayer[M, Inner]): MonadLift[Inner, M] = {
    new MonadLift[Inner, M] {
      def lift[A](ba: Inner[A]): M[A] = layer.layer(ba)
    }
  }

  implicit def liftComposeInd[M[_], B[_], B2[_]]
  (implicit liftF1: MonadLift[B, M], liftF2: MonadLift[B2, B]): MonadLift[B2, M] = {
    new MonadLift[B2, M] {
      def lift[A](ba: B2[A]): M[A] = liftF1.lift(liftF2.lift(ba))
    }
  }
}

