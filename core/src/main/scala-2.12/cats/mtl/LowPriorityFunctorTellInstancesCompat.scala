package cats.mtl

import cats.{Functor, Monoid}
import cats.data.Writer

private[mtl] trait LowPriorityFunctorTellInstancesCompat {
  implicit def functorTellForWriter[L: Monoid]: FunctorTell[Writer[L, *], L] =
    new FunctorTell[Writer[L, *], L] {
      val functor = Functor[Writer[L, *]]
      def tell(l: L) = Writer.tell[L](l)
    }
}
