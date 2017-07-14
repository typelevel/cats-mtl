package cats
package mtl
package instances

import cats.data.WriterT
import cats.syntax.functor._

trait ListenInstances extends ListenInstancesLowPriority {

  implicit final def listenInd[M[_], L, Inner[_], State[_]](implicit
                                                            layer: MonadLayerControl.Aux[M, Inner, State],
                                                            under: FunctorListen[Inner, L]
                                                           ): FunctorListen[M, L] = {
    new FunctorListen[M, L] {
      val tell: FunctorTell[M, L] = cats.mtl.instances.tell.tellInd(layer, under.tell)

      def listen[A](fa: M[A]): M[(A, L)] = {
        layer.outerInstance.flatMap(layer.layerControl { cps =>
          under.listen(cps(fa))
        })(x =>
          layer.outerInstance.map(layer.restore(x._1))(z => (z, x._2))
        )
      }

      def listens[A, B](fa: M[A])(f: (L) => B): M[(A, B)] = {
        layer.outerInstance.map(listen(fa)) { case (a, l) =>
          (a, f(l))
        }
      }

    }
  }

  implicit final def listenWriterId[L]
  (implicit L: Monoid[L]
  ): FunctorListen[WriterTC[Id, L]#l, L] = {
    listenWriter[Id, L]
  }
}

trait ListenInstancesLowPriority {
  implicit final def listenWriter[M[_], L]
  (implicit M: Monad[M], L: Monoid[L]
  ): FunctorListen[WriterTC[M, L]#l, L] = {
    new FunctorListen[WriterTC[M, L]#l, L] {
      val tell = instances.tell.tellWriter[M, L]

      def listen[A](fa: WriterT[M, L, A]): WriterT[M, L, (A, L)] = {
        WriterT[M, L, (A, L)](fa.run.map { case (l, a) => (l, (a, l)) })
      }

      def listens[A, B](fa: WriterT[M, L, A])(f: (L) => B): WriterT[M, L, (A, B)] = {
        WriterT[M, L, (A, B)](fa.run.map { case (l, a) => (l, (a, f(l))) })
      }

    }
  }

  implicit final def listenTuple[L]
  (implicit L: Monoid[L]
  ): FunctorListen[TupleC[L]#l, L] = {
    new FunctorListen[TupleC[L]#l, L] {
      val tell = instances.tell.tellTuple[L]

      def listen[A](fa: (L, A)): (L, (A, L)) = {
        val (l, a) = fa
        (l, (a, l))
      }

      def listens[A, B](fa: (L, A))(f: (L) => B): (L, (A, B)) = {
        val (l, a) = fa
        (l, (a, f(l)))
      }
    }
  }

}

object listen extends ListenInstances
