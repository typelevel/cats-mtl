package cats
package mtl
package instances

import cats.mtl.lifting.MonadLayerControl

trait ListenInstances {

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
}

object listen extends ListenInstances
