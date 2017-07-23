package cats
package mtl
package instances

import cats.data.ReaderT

trait LocalInstances extends LocalLowPriorityInstances {
  implicit final def localInd[M[_], Inner[_], E](implicit ml: MonadLayer[M, Inner],
                                                 under: ApplicativeLocal[Inner, E]
                                                ): ApplicativeLocal[M, E] = {
    new ApplicativeLocal[M, E] {
      val ask: ApplicativeAsk[M, E] =
        instances.ask.askLayerInd[M, Inner, E](ml, under.ask)

      def local[A](f: E => E)(fa: M[A]): M[A] = {
        ml.outerInstance.flatMap(ask.ask)(r =>
          ml.layerImapK(fa)(new (Inner ~> Inner) {
            def apply[X](fa: Inner[X]): Inner[X] = under.local(f)(fa)
          }, new (Inner ~> Inner) {
            def apply[X](fa: Inner[X]): Inner[X] = under.scope(r)(fa)
          }))
      }

      def scope[A](e: E)(fa: M[A]): M[A] = {
        ml.outerInstance.flatMap(ask.ask)(r =>
          ml.layerImapK(fa)(new (Inner ~> Inner) {
            def apply[X](fa: Inner[X]): Inner[X] = under.scope(e)(fa)
          }, new (Inner ~> Inner) {
            def apply[X](fa: Inner[X]): Inner[X] = under.scope(r)(fa)
          }))
      }

    }
  }

  implicit final def localReaderId[E]: ApplicativeLocal[ReaderTC[Id, E]#l, E] = {
    localReader[Id, E]
  }

}

private[instances] trait LocalLowPriorityInstances {
  implicit final def localReader[M[_], E](implicit M: Applicative[M]): ApplicativeLocal[CurryT[ReaderTCE[E]#l, M]#l, E] = {
    new ApplicativeLocal[ReaderTC[M, E]#l, E] {
      val ask: ApplicativeAsk[ReaderTC[M, E]#l, E] =
        instances.ask.askReader[M, E]

      def local[A](f: E => E)(fa: ReaderT[M, E, A]): ReaderT[M, E, A] = ReaderT.local(f)(fa)

      def scope[A](e: E)(fa: ReaderT[M, E, A]): ReaderT[M, E, A] = ReaderT(_ => fa.run(e))
    }
  }

  implicit final def localFunction[E]: ApplicativeLocal[FunctionC[E]#l, E] = {
    new ApplicativeLocal[FunctionC[E]#l, E] {
      val ask: ApplicativeAsk[FunctionC[E]#l, E] =
        instances.ask.askFunction[E]

      def local[A](f: E => E)(fa: E => A): E => A = fa.compose(f)

      def scope[A](e: E)(fa: E => A): E => A = _ => fa(e)
    }
  }
}

object local extends LocalInstances
