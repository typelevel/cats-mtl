package cats
package mtl
package instances

import cats.data.WriterT
import cats.syntax.functor._

trait ListenInstances extends ListenLowPriorityInstances {
  implicit def localInd[M[_], Inner[_], L](implicit
                                           lift: MonadLayer.Aux[M, Inner],
                                           under: Listen[Inner, L]
                                          ): Listen[M, L] =
    new Listen[M, L] {
      val monad = lift.monad

      val tell = instances.tell.tellInd[M, Inner, L](lift, under.tell)

      def listen[A](fa: M[A]): M[(A, L)] = lift.monad.flatMap(fa) { a =>
        lift.layer(under.listen(lift.innerMonad.pure(a)))
      }

      def pass[A](fa: M[(A, (L) => L)]): M[A] = lift.monad.flatMap(fa) { a =>
        lift.layer(under.pass(lift.innerMonad.pure(a)))
      }
    }
}

trait ListenLowPriorityInstances {
  implicit def localWriter[M[_], L](implicit M: Monad[M], L: Monoid[L]): Listen[WriterTC[M, L]#l, L] =
    new Listen[WriterTC[M, L]#l, L] {
      val monad = WriterT.catsDataMonadWriterForWriterT(M, L)

      val tell = instances.tell.tellWriter[M, L]

      def listen[A](fa: WriterT[M, L, A]): WriterT[M, L, (A, L)] = {
        WriterT[M, L, (A, L)](fa.run.map { case (l, a) => (l, (a, l)) })
      }

      def pass[A](fa: WriterT[M, L, (A, (L) => L)]): WriterT[M, L, A] = {
        WriterT[M, L, A](fa.run.map { case (l, (a, f)) => (f(l), a) })
      }
    }
}

object listen {

}
