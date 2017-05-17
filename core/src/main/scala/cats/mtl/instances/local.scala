package cats
package mtl
package instances

import cats.data.ReaderT

trait LocalInstances extends LocalLowPriorityInstances {
  implicit def localNInd[M[_], Inner[_], E](implicit ml: MonadLayer.Aux[M, Inner],
                                            under: Local[Inner, E]
                                           ): Local[M, E] = {
    new Local[M, E] {
      val monad = ml.monad
      val ask: Ask[M, E] =
        instances.ask.askInd[M, Inner, E](ml, under.ask)

      def local[A](fa: M[A])(f: (E) => E): M[A] = {
        ml.monad.flatMap(ask.ask)(r =>
          ml.imapK(fa)(new (Inner ~> Inner) {
            def apply[A](fa: Inner[A]): Inner[A] = under.local(fa)(f)
          }, new (Inner ~> Inner) {
            def apply[A](fa: Inner[A]): Inner[A] = under.local(fa)(_ => r)
          }))
      }
    }
  }
}

trait LocalLowPriorityInstances {

  implicit def localNReader[M[_], E](implicit M: Monad[M]): Local[CurryT[ReaderTCE[E]#l, M]#l, E] = {
    new Local[CurryT[ReaderTCE[E]#l, M]#l, E] {
      val monad = M
      val ask: Ask[CurryT[ReaderTCE[E]#l, M]#l, E] =
        instances.ask.askReader[M, E]

      def local[A](fa: ReaderT[M, E, A])(f: (E) => E): ReaderT[M, E, A] = {
        ReaderT.local(f)(fa)
      }
    }
  }

}

object local extends LocalInstances
