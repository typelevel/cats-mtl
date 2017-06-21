package cats
package mtl
package instances

import cats.data.ReaderT

trait ReaderTInstances extends ReaderTInstancesLowPriority {
  implicit final def readerMonadLayer[M[_], E]
  (implicit M: Monad[M]): monad.Layer[ReaderTC[M, E]#l, M] = {
    readerMonadTransControl[M, E]
  }
}

private[instances] trait ReaderTInstancesLowPriority {
  implicit final def readerMonadTransControl[M[_], E]
  (implicit M: Monad[M]): monad.LayerControl.Aux[ReaderTC[M, E]#l, M, Id] = {
    new monad.LayerControl[ReaderTC[M, E]#l, M] {
      type State[A] = Id[A]

      val outerInstance: Monad[ReaderTC[M, E]#l] =
        ReaderT.catsDataMonadReaderForKleisli

      val innerInstance: Monad[M] = M

      def layerMapK[A](ma: ReaderT[M, E, A])(trans: M ~> M): ReaderT[M, E, A] = ma.transform(trans)

      def layer[A](inner: M[A]): ReaderT[M, E, A] = ReaderT.lift(inner)

      def restore[A](state: Id[A]): ReaderT[M, E, A] = ReaderT.pure(state)

      def layerControl[A](cps: (ReaderTC[M, E]#l ~> (M of Id)#l) => M[A]): ReaderT[M, E, A] = {
        ReaderT[M, E, A]((e: E) =>
          cps(new (ReaderTC[M, E]#l ~> M) {
            def apply[X](fa: ReaderT[M, E, X]): M[X] = fa.run(e)
          })
        )
      }

      def zero[A](state: Id[A]): Boolean = false
    }
  }

}

object readert extends ReaderTInstances
