package cats
package mtl
package instances

import cats.data.WriterT
import cats.mtl.lifting.{ApplicativeLayerFunctor, FunctorLayerFunctor, MonadLayerControl}
import cats.syntax.functor._

trait WriterTInstances extends WriterTInstances1 {
  implicit def writerFunctorLayerFunctor[M[_], L]
  (implicit L: Monoid[L], M: Functor[M]): FunctorLayerFunctor[WriterTC[M, L]#l, M] = {
    new FunctorLayerFunctor[WriterTC[M, L]#l, M] {
      def layerMapK[A](ma: WriterT[M, L, A])(trans: M ~> M): WriterT[M, L, A] = WriterT(trans(ma.run))

      val outerInstance: Functor[WriterTC[M, L]#l] = WriterT.catsDataCoflatMapForWriterT[M, L]
      val innerInstance: Functor[M] = M

      def layer[A](inner: M[A]): WriterT[M, L, A] = {
        WriterT(M.map(inner)(v => (L.empty, v)))
      }
    }
  }
}

private[instances] trait WriterTInstances1 extends WriterTInstances2 {
  implicit def writerApplicativeLayerFunctor[M[_], L]
  (implicit L: Monoid[L], M: Applicative[M]): ApplicativeLayerFunctor[WriterTC[M, L]#l, M] = {
    new ApplicativeLayerFunctor[WriterTC[M, L]#l, M] {
      def layerMapK[A](ma: WriterT[M, L, A])(trans: M ~> M): WriterT[M, L, A] = WriterT(trans(ma.run))

      val outerInstance: Applicative[WriterTC[M, L]#l] = WriterT.catsDataApplicativeForWriterT(M, L)
      val innerInstance: Applicative[M] = M

      def layer[A](inner: M[A]): WriterT[M, L, A] = WriterT.liftF(inner)
    }
  }
}

private[instances] trait WriterTInstances2 {
  implicit final def writerMonadLayerControl[M[_], L]
  (implicit L: Monoid[L], M: Monad[M]): MonadLayerControl.Aux[WriterTC[M, L]#l, M, TupleC[L]#l] = {
    new MonadLayerControl[WriterTC[M, L]#l, M] {
      type State[A] = (L, A)

      val outerInstance: Monad[WriterTC[M, L]#l] =
        WriterT.catsDataMonadForWriterT

      val innerInstance: Monad[M] = M

      def layerMapK[A](ma: WriterT[M, L, A])(trans: M ~> M): WriterT[M, L, A] = WriterT(trans(ma.run))

      def layer[A](inner: M[A]): WriterT[M, L, A] = WriterT.liftF(inner)

      def restore[A](state: (L, A)): WriterT[M, L, A] = WriterT(M.pure(state))

      def layerControl[A](cps: (WriterTC[M, L]#l ~> (M of TupleC[L]#l)#l) => M[A]): WriterT[M, L, A] = {
        WriterT(cps(new (WriterTC[M, L]#l ~> (M of TupleC[L]#l)#l) {
          def apply[X](fa: WriterT[M, L, X]): M[(L, X)] = fa.run
        }).map(x => (L.empty, x)))
      }

      def zero[A](state: (L, A)): Boolean = false
    }
  }
}

object writert extends WriterTInstances

