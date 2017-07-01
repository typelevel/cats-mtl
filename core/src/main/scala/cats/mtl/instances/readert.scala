package cats
package mtl
package instances

import cats.data.{Kleisli, ReaderT}

trait ReaderTInstances extends ReaderTInstances1 {
  implicit def readerFunctorLayerFunctor[M[_], E]
  (implicit M: Functor[M]): FunctorLayerFunctor[ReaderTC[M, E]#l, M] = {
    new FunctorLayerFunctor[ReaderTC[M, E]#l, M] {
      def layerMapK[A](ma: ReaderT[M, E, A])(trans: M ~> M): ReaderT[M, E, A] = ma.transform(trans)

      val outerInstance: Functor[ReaderTC[M, E]#l] = Kleisli.catsDataFunctorForKleisli(M)
      val innerInstance: Functor[M] = M

      def layer[A](inner: M[A]): ReaderT[M, E, A] = ReaderT.lift(inner)
    }
  }
}

trait ReaderTInstances1 extends ReaderTInstances2 {
  implicit def readerApplicativeLayerFunctor[M[_], E]
  (implicit M: Applicative[M]): ApplicativeLayerFunctor[ReaderTC[M, E]#l, M] = {
    new ApplicativeLayerFunctor[ReaderTC[M, E]#l, M] {
      def layerMapK[A](ma: ReaderT[M, E, A])(trans: M ~> M): ReaderT[M, E, A] = ma.transform(trans)

      val outerInstance: Applicative[ReaderTC[M, E]#l] = Kleisli.catsDataApplicativeForKleisli(M)
      val innerInstance: Applicative[M] = M

      def layer[A](inner: M[A]): ReaderT[M, E, A] = ReaderT.lift(inner)
    }
  }
}

trait ReaderTInstances2 {
  implicit final def readerMonadLayerControl[M[_], E]
  (implicit M: Monad[M]): MonadLayerControl.Aux[ReaderTC[M, E]#l, M, Id] = {
    new MonadLayerControl[ReaderTC[M, E]#l, M] {
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
