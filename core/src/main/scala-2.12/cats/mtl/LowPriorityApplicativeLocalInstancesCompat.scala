package cats.mtl

import cats.{Applicative, Id}
import cats.data.{Kleisli, Reader}

private[mtl] trait LowPriorityApplicativeLocalInstancesCompat {
  implicit def applicativeLocalForReader[E]: ApplicativeLocal[Reader[E, *], E] =
    new ApplicativeLocal[Reader[E, *], E] {
      def local[A](f: E => E)(fa: Reader[E, A]) = fa.local(f)
      val applicative = Applicative[Reader[E, *]]
      def ask = Kleisli.ask[Id, E]
    }
}
