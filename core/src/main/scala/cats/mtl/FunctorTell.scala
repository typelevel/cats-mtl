package cats
package mtl

import cats.data.WriterT

/**
 * `FunctorTell[F, L]` is the ability to "log" values `L` inside a context `F[_]`, as an effect.
 *
 * `FunctorTell` has no external laws.
 *
 * `FunctorTell` has one internal law:
 * {{{
 * def writerIsTellAndMap(a: A, l: L) = {
 *   (tell(l) as a) <-> writer(a, l)
 * }
 *
 * def tupleIsWriterFlipped(a: A, l: L) = {
 *   writer(a, l) <-> tuple((l, a))
 * }
 * }}}
 */
trait FunctorTell[F[_], L] extends Serializable {
  def functor: Functor[F]

  def tell(l: L): F[Unit]

  def writer[A](a: A, l: L): F[A] = functor.as(tell(l), a)

  def tuple[A](ta: (L, A)): F[A] = writer(ta._2, ta._1)
}

private[mtl] trait FunctorTellMonadPartialOrder[F[_], G[_], L] extends FunctorTell[G, L] {
  val lift: MonadPartialOrder[F, G]
  val F: FunctorTell[F, L]

  override val functor = lift.monadG
  override def tell(l: L) = lift(F.tell(l))
}

private[mtl] trait LowPriorityFunctorTellInstances {

  implicit def functorTellForPartialOrder[F[_], G[_], L](
      implicit lift0: MonadPartialOrder[F, G],
      F0: FunctorTell[F, L])
      : FunctorTell[G, L] =
    new FunctorTellMonadPartialOrder[F, G, L] {
      val lift: MonadPartialOrder[F, G] = lift0
      val F: FunctorTell[F, L] = F0
    }
}

private[mtl] trait FunctorTellInstances extends LowPriorityFunctorTellInstances with LowPriorityFunctorTellInstancesCompat {

  implicit def functorTellForWriterT[F[_]: Applicative, L: Monoid]: FunctorTell[WriterT[F, L, *], L] =
    new FunctorTell[WriterT[F, L, *], L] {
      val functor = Functor[WriterT[F, L, *]]
      def tell(l: L) = WriterT.tell[F, L](l)
    }
}

object FunctorTell extends FunctorTellInstances {
  def apply[F[_], L](implicit tell: FunctorTell[F, L]): FunctorTell[F, L] = tell
  def tell[F[_], L](l: L)(implicit tell: FunctorTell[F, L]): F[Unit] = tell.tell(l)

  def tellF[F[_]]: tellFPartiallyApplied[F] = new tellFPartiallyApplied[F]

  final private[mtl] class tellFPartiallyApplied[F[_]](val dummy: Boolean = false) extends AnyVal {
    @inline def apply[L](l: L)(implicit tell: FunctorTell[F, L]): F[Unit] = {
      tell.tell(l)
    }
  }
}
