package cats
package mtl
package instances

import cats.data.{IndexedReaderWriterStateT, ReaderWriterStateT, WriterT}
import cats.mtl.lifting.MonadLayerControl
import cats.syntax.functor._

trait ListenInstances extends ListenInstancesLowPriority {

  implicit final def listenInd[M[_], L, Inner[_], State[_]](implicit
                                                            layer: MonadLayerControl.Aux[M, Inner, State],
                                                            under: FunctorListen[Inner, L]
                                                           ): FunctorListen[M, L] = {
    new FunctorListen[M, L] {
      val functor: Functor[M] = layer.outerInstance

      def tell(l: L): M[Unit] = layer.layer(under.tell(l))

      def writer[A](a: A, l: L): M[A] = layer.layer(under.writer(a, l))

      def tuple[A](ta: (L, A)): M[A] = layer.layer(under.tuple(ta))


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
      val functor = new Functor[WriterTC[M, L]#l] {
        def map[A, B](fa: WriterT[M, L, A])(f: (A) => B): WriterT[M, L, B] = fa.map(f)
      }

      def tell(l: L): WriterT[M, L, Unit] = WriterT.tell(l)

      def writer[A](a: A, l: L): WriterT[M, L, A] = WriterT.put(a)(l)

      def tuple[A](ta: (L, A)): WriterT[M, L, A] = WriterT(M.pure(ta))

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
      val functor = cats.instances.tuple.catsStdMonadForTuple2

      def tell(l: L): (L, Unit) = (l, ())

      def writer[A](a: A, l: L): (L, A) = (l, a)

      def tuple[A](ta: (L, A)): (L, A) = ta

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

  implicit final def listenReaderWriterState[M[_], R, L, S]
  (implicit L: Monoid[L], M: Monad[M]): FunctorListen[ReaderWriterStateT[M, R, L, S, ?], L] =
    new DefaultFunctorListen[ReaderWriterStateT[M, R, L, S, ?], L] {
      val functor: Functor[ReaderWriterStateT[M, R, L, S, ?]] =
        IndexedReaderWriterStateT.catsDataMonadForRWST

      def tell(l: L): ReaderWriterStateT[M, R, L, S, Unit] = ReaderWriterStateT.tell(l)

      def listen[A](fa: ReaderWriterStateT[M, R, L, S, A]): ReaderWriterStateT[M, R, L, S, (A, L)] =
        ReaderWriterStateT((e, s) => fa.run(e, s).map {
          case (l, s, a) => (l, s, (a, l))
        })
    }
}

object listen extends ListenInstances
