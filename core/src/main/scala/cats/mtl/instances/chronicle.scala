package cats
package mtl
package instances

import cats.data.{Ior, IorT}
import cats.mtl.lifting.MonadLayerControl
import cats.syntax.functor._

trait ChronicleInstances extends ChronicleLowPriorityInstances {
  implicit final def chronicleInd[M[_], Inner[_], E](implicit ml: MonadLayerControl[M, Inner],
                                                     under: MonadChronicle[Inner, E]
                                                    ): MonadChronicle[M, E] = {
    new DefaultMonadChronicle[M, E] {
      val monad: Monad[M] =
        ml.outerInstance

      def dictate(c: E): M[Unit] = {
        ml.layer(under.dictate(c))
      }

      def confess[A](c: E): M[A] =
        ml.layer(under.confess(c))

      def materialize[A](fa: M[A]): M[Ior[E, A]] =
        ml.outerInstance.flatMap(ml.layerControl(nt =>
          under.materialize(nt(fa))))(_.traverse(ml.restore)(ml.outerInstance))
    }
  }
}

trait ChronicleLowPriorityInstances {
  implicit final def chronicleIorT[F[_], E](implicit S: Semigroup[E],
                                            F: Monad[F]): MonadChronicle[IorTC[F, E]#l, E] =
    new DefaultMonadChronicle[IorTC[F, E]#l, E] {
      override val monad: Monad[IorTC[F, E]#l] = IorT.catsDataMonadErrorForIorT

      override def dictate(c: E): IorT[F, E, Unit] = IorT.bothT[F](c, ())

      override def confess[A](c: E): IorT[F, E, A] = IorT.leftT[F, A](c)

      override def materialize[A](fa: IorT[F, E, A]): IorT[F, E, E Ior A] = IorT[F, E, E Ior A] {
        fa.value.map {
          case Ior.Left(e)    => Ior.right(Ior.left(e))
          case Ior.Right(a)   => Ior.right(Ior.right(a))
          case Ior.Both(e, a) => Ior.right(Ior.both(e, a))
        }
      }
    }

  implicit final def chronicleIor[E](implicit S: Semigroup[E]): MonadChronicle[IorC[E]#l, E] =
    new DefaultMonadChronicle[IorC[E]#l, E] {
      override val monad: Monad[IorC[E]#l] = Ior.catsDataMonadErrorForIor

      override def dictate(c: E): Ior[E, Unit] = Ior.both(c, ())

      override def confess[A](c: E): Ior[E, A] = Ior.left(c)

      override def materialize[A](fa: Ior[E, A]): Ior[E, Ior[E, A]] = fa match {
        case Ior.Left(e)    => Ior.right(Ior.left(e))
        case Ior.Right(a)   => Ior.right(Ior.right(a))
        case Ior.Both(e, a) => Ior.right(Ior.both(e, a))
      }
    }
}

object chronicle extends ChronicleInstances
