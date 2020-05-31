package cats.mtl

import cats.{Applicative, Id}
import cats.data.{Kleisli, Reader}

private[mtl] trait LowPriorityApplicativeAskInstancesCompat {
  implicit def applicativeAskForReader[E]: ApplicativeAsk[Reader[E, *], E] =
    new ApplicativeAsk[Reader[E, *], E] {
      val applicative = Applicative[Reader[E, *]]
      def ask = Kleisli.ask[Id, E]
    }
}
