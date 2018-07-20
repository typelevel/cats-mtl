package cats
package mtl
package instances

import cats.data.{Ior, IorT}

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

trait IorTInstances1 extends IorTInstancesLowPriority1 {
  implicit def iorMonadLayerFunctor[M[_], A](
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

private[instances] trait IorTInstancesLowPriority1 {
  final implicit def chronicleIorT[F[_], E](implicit S: Semigroup[E],
                                            F: Monad[F]): MonadChronicle[IorTC[F, E]#l, E] =
    new DefaultMonadChronicle[IorTC[F, E]#l, E] {
      override val monad: Monad[IorTC[F, E]#l] = IorT.catsDataMonadErrorForIorT

      override def dictate(c: E): IorT[F, E, Unit] = IorT.left(F.pure(c))

      override def confess[A](c: E): IorT[F, E, A] = IorT.left(F.pure(c))

      override def materialize[A](fa: IorT[F, E, A]): IorT[F, E, E Ior A] = IorT[F, E, E Ior A] {
        F.map(fa.value) {
          case Ior.Left(e)    => Ior.left(e)
          case Ior.Right(a)   => Ior.right(Ior.right(a))
          case Ior.Both(e, a) => Ior.both(e, Ior.right(a))
        }
      }
    }
}

object iort extends IorTInstances
