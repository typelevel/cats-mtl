package cats
package mtl
package instances

import cats.data.ReaderT
import cats.mtl.monad.{Asking, Layer, Scoping}

trait ScopingInstances extends ScopingLowPriorityInstances {
  implicit def scopingNInd[M[_], Inner[_], E](implicit ml: Layer[M, Inner],
                                              under: Scoping[Inner, E]
                                           ): Scoping[M, E] = {
    new Scoping[M, E] {
      val ask: Asking[M, E] =
        instances.asking.askInd[M, Inner, E](ml, under.ask)

      def local[A](fa: M[A])(f: (E) => E): M[A] = {
        ml.monad.flatMap(ask.ask)(r =>
          ml.layerImapK(fa)(new (Inner ~> Inner) {
            def apply[X](fa: Inner[X]): Inner[X] = under.local(fa)(f)
          }, new (Inner ~> Inner) {
            def apply[X](fa: Inner[X]): Inner[X] = under.scope(fa)(r)
          }))
      }

      def scope[A](fa: M[A])(e: E): M[A] = {
        ml.monad.flatMap(ask.ask)(r =>
          ml.layerImapK(fa)(new (Inner ~> Inner) {
            def apply[X](fa: Inner[X]): Inner[X] = under.scope(fa)(e)
          }, new (Inner ~> Inner) {
            def apply[X](fa: Inner[X]): Inner[X] = under.scope(fa)(r)
          }))
      }

    }
  }
}

trait ScopingLowPriorityInstances {

  implicit def scopingNReader[M[_], E](implicit M: Monad[M]): Scoping[CurryT[ReaderTCE[E]#l, M]#l, E] = {
    new Scoping[CurryT[ReaderTCE[E]#l, M]#l, E] {
      val ask: Asking[CurryT[ReaderTCE[E]#l, M]#l, E] =
        instances.asking.askReader[M, E]

      def local[A](fa: ReaderT[M, E, A])(f: (E) => E): ReaderT[M, E, A] = {
        ReaderT.local(f)(fa)
      }

      def scope[A](fa: ReaderT[M, E, A])(e: E): ReaderT[M, E, A] = {
        ReaderT((_: E) => fa.run(e))
      }
    }
  }

}

object scoping extends ScopingInstances
