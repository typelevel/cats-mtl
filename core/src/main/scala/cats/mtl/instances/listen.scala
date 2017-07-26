package cats
package mtl
package instances

import cats.data.WriterT
import cats.syntax.functor._

trait ListenInstances extends ListenInstancesLowPriority {

  final def tellInd[M[_], Inner[_], L](implicit
                                                lift: ApplicativeLayer[M, Inner],
                                                under: FunctorTell[Inner, L]
                                               ): FunctorTell[M, L] = {
    new FunctorTell[M, L] {
      val functor: Functor[M] = lift.outerInstance

      def tell(l: L): M[Unit] = lift.layer(under.tell(l))

      def writer[A](a: A, l: L): M[A] = lift.layer(under.writer(a, l))

      def tuple[A](ta: (L, A)): M[A] = lift.layer(under.tuple(ta))
    }
  }

  implicit final def listenInd[M[_], L, Inner[_], State[_]](implicit
                                                            layer: MonadLayerControl.Aux[M, Inner, State],
                                                            under: FunctorListen[Inner, L]
                                                           ): FunctorListen[M, L] = {
    new FunctorListen[M, L] {
      val tell: FunctorTell[M, L] = tellInd(layer, under.tell)

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
      val tell = tellWriter[M, L]

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
      val tell = tellTuple[L]

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

  final def tellWriter[M[_], L](implicit L: Monoid[L], M: Applicative[M]): FunctorTell[CurryT[WriterTCL[L]#l, M]#l, L] = {
    new FunctorTell[CurryT[WriterTCL[L]#l, M]#l, L] {
      val functor = new Functor[WriterTC[M, L]#l] {
        def map[A, B](fa: WriterT[M, L, A])(f: (A) => B): WriterT[M, L, B] = fa.map(f)
      }

      def tell(l: L): WriterT[M, L, Unit] = WriterT.tell(l)

      def writer[A](a: A, l: L): WriterT[M, L, A] = WriterT.put(a)(l)

      def tuple[A](ta: (L, A)): WriterT[M, L, A] = WriterT(M.pure(ta))
    }
  }

  final def tellTuple[L](implicit L: Monoid[L]): FunctorTell[TupleC[L]#l, L] = {
    new FunctorTell[TupleC[L]#l, L] {
      val functor = cats.instances.tuple.catsStdMonadForTuple2

      def tell(l: L): (L, Unit) = (l, ())

      def writer[A](a: A, l: L): (L, A) = (l, a)

      def tuple[A](ta: (L, A)): (L, A) = ta
    }
  }

}

object listen extends ListenInstances
