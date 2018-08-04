package cats
package mtl
package instances

import cats.data.{Ior, IorT}
import cats.syntax.functor._

trait IorTInstances extends IorTInstances1 {
  implicit def iorFunctorLayerFunctor[M[_], A](
      implicit M: Functor[M]): FunctorLayerFunctor[IorTC[M, A]#l, M] = {
    new FunctorLayerFunctor[IorTC[M, A]#l, M] {
      override def layerMapK[B](ma: IorT[M, A, B])(trans: M ~> M): IorT[M, A, B] = ma.mapK(trans)

      override val outerInstance: Functor[IorTC[M, A]#l] = IorT.catsDataFunctorForIorT[M, A](M)
      override val innerInstance: Functor[M] = M

      override def layer[B](inner: M[B]): IorT[M, A, B] = IorT.right(inner)
    }
  }
}

trait IorTInstances1 {
  implicit def iorMonadLayerControl[M[_], A](
      implicit M: Monad[M],
      S: Semigroup[A]): MonadLayerControl.Aux[IorTC[M, A]#l, M, IorC[A]#l] =
    new MonadLayerControl[IorTC[M, A]#l, M] {
      override type State[B] = Ior[A, B]

      override def restore[B](state: Ior[A, B]): IorT[M, A, B] = IorT(M.pure(state))
      override def layerControl[B](
          cps: (IorTC[M, A]#l ~> (M of IorC[A]#l)#l) => M[B]): IorT[M, A, B] = {
        IorT[M, A, B] {
          cps(new (IorTC[M, A]#l ~> (M of IorC[A]#l)#l) {
            def apply[X](fa: IorT[M, A, X]): M[Ior[A, X]] = fa.value
          }).map(b => Ior.right[A, B](b))
        }
      }

      override def zero[B](state: Ior[A, B]): Boolean = state.isLeft

      override def layerMapK[B](ma: IorT[M, A, B])(trans: M ~> M): IorT[M, A, B] = ma.mapK(trans)

      override val outerInstance: Monad[IorTC[M, A]#l] = IorT.catsDataMonadErrorForIorT
      override val innerInstance: Monad[M] = M

      override def layer[B](inner: M[B]): IorT[M, A, B] = IorT.liftF(inner)
    }
}

object iort extends IorTInstances
