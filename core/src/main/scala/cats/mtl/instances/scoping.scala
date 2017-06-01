package cats
package mtl
package instances

import cats.data.ReaderT

trait ScopingInstances extends ScopingLowPriorityInstances {
  implicit def scopingNInd[M[_], Inner[_], E](implicit ml: monad.Layer[M, Inner],
                                              under: monad.Scoping[Inner, E]
                                             ): monad.Scoping[M, E] = {
    new monad.Scoping[M, E] {
      val ask: monad.Asking[M, E] =
        instances.asking.askInd[M, Inner, E](ml, under.ask)

      def local[A](fa: M[A])(f: (E) => E): M[A] = {
        ml.outerMonad.flatMap(ask.ask)(r =>
          ml.layerImapK(fa)(new (Inner ~> Inner) {
            def apply[X](fa: Inner[X]): Inner[X] = under.local(fa)(f)
          }, new (Inner ~> Inner) {
            def apply[X](fa: Inner[X]): Inner[X] = under.scope(fa)(r)
          }))
      }

      def scope[A](fa: M[A])(e: E): M[A] = {
        ml.outerMonad.flatMap(ask.ask)(r =>
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

  implicit def scopingNReader[M[_], E](implicit M: Monad[M]): monad.Scoping[CurryT[ReaderTCE[E]#l, M]#l, E] = {
    new monad.Scoping[CurryT[ReaderTCE[E]#l, M]#l, E] {
      val ask: monad.Asking[CurryT[ReaderTCE[E]#l, M]#l, E] =
        instances.asking.askReader[M, E]

      def local[A](fa: ReaderT[M, E, A])(f: (E) => E): ReaderT[M, E, A] = ReaderT.local(f)(fa)

      def scope[A](fa: ReaderT[M, E, A])(e: E): ReaderT[M, E, A] = ReaderT(_ => fa.run(e))
    }
  }

}

object scoping extends ScopingInstances
