package cats
package mtl

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
  val functor: Functor[F]

  def tell(l: L): F[Unit]

  def writer[A](a: A, l: L): F[A]

  def tuple[A](ta: (L, A)): F[A]
}

object FunctorTell {
  def apply[F[_], L](implicit tell: FunctorTell[F, L]): FunctorTell[F, L] = tell
  def tell[F[_], L](l: L)(implicit tell: FunctorTell[F, L]): F[Unit] = tell.tell(l)

  def tellF[F[_]]: tellFPartiallyApplied[F] = new tellFPartiallyApplied[F]

  final private[mtl] class tellFPartiallyApplied[F[_]](val dummy: Boolean = false) extends AnyVal {
    @inline def apply[L](l: L)(implicit tell: FunctorTell[F, L]): F[Unit] = {
      tell.tell(l)
    }
  }

  abstract class FunctorTellTemplate[F[_], L](implicit override val functor: Functor[F]) extends FunctorTell[F, L]
}

