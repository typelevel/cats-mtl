package cats
package mtl

import cats.data.{WriterT, ReaderWriterStateT => RWST}
import cats.data.IndexedReaderWriterStateT

trait ApplicativeCensor[F[_], L] extends FunctorListen[F, L] {
  val applicative: Applicative[F]
  val monoid: Monoid[L]
  override lazy val functor: Functor[F] = applicative

  def censor[A](fa: F[A])(f: L => L): F[A]

  def clear[A](fa: F[A]): F[A] = censor(fa)(_ => monoid.empty)
}

object ApplicativeCensor {
  def apply[F[_], L](implicit ev: ApplicativeCensor[F, L]): ApplicativeCensor[F, L] = ev
}
/*
private trait LowPriorityApplicativeCensorInstances {
  implicit def inductiveApplicativeCensorForWriterT[F[_]: Applicative, L, L0: Monoid](
      implicit F: ApplicativeCensor[F, L])
      : ApplicativeCensor[WriterT[F, L0, *], L] =
    new ApplicativeCensor[WriterT[F, L0, *], L] {


      def censor[A](fa: WriterT[F,L0,A])(f: L => L): WriterT[F,L0,A] = 
        WriterT(F.censor(fa.run)(f))


      val applicative = Applicative[WriterT[F, L0, *]]

    }

}

private[mtl] trait ApplicativeCensorInstances extends LowPriorityApplicativeCensorInstances {

}
*/

