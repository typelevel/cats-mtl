package cats
package mtl
package instances

import cats.data.{Ior, IorT}
import cats.syntax.functor._

trait ChronicleInstances extends ChronicleLowPriorityInstances

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
