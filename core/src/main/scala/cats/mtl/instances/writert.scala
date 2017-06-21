package cats
package mtl
package instances

import cats.data.WriterT
import cats.syntax.functor._

trait WriterTInstances extends WriterTInstancesLowPriority {
  implicit final def writerMonadLayer[M[_], L]
  (implicit L: Monoid[L], M: Monad[M]): monad.Layer[WriterTC[M, L]#l, M] = {
    writerMonadLayerControl[M, L]
  }
}

private[instances] trait WriterTInstancesLowPriority {
  implicit final def writerMonadLayerControl[M[_], L]
  (implicit L: Monoid[L], M: Monad[M]): monad.LayerControl.Aux[WriterTC[M, L]#l, M, TupleC[L]#l] = {
    new monad.LayerControl[WriterTC[M, L]#l, M] {
      type State[A] = (L, A)

      val outerInstance: Monad[CurryT[WriterTCL[L]#l, M]#l] =
        WriterT.catsDataMonadWriterForWriterT

      val innerInstance: Monad[M] = M

      def layerMapK[A](ma: WriterT[M, L, A])(trans: M ~> M): WriterT[M, L, A] = WriterT(trans(ma.run))

      def layer[A](inner: M[A]): WriterT[M, L, A] = WriterT.lift(inner)

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

