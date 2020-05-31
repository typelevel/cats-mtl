package cats.mtl

import cats.{Functor, Id, Monoid}
import cats.data.Writer

private[mtl] trait LowPriorityFunctorListenInstancesCompat {
  implicit def baseFunctorListenForWriter[L]: FunctorListen[Writer[L, *], L] =
    new FunctorListenWriterT[Id, L] {}
}
