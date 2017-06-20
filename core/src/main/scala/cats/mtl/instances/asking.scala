package cats
package mtl
package instances

import cats.data.{Kleisli, ReaderT}
import cats.mtl.applicative.Asking

trait AskingInstances extends AskingInstancesLowPriority {
  implicit final def askIndT[Inner[_], Outer[_[_], _], E]
  (implicit lift: applicative.Trans.Aux[CurryT[Outer, Inner]#l, Inner, Outer],
   under: Asking[Inner, E]): Asking[CurryT[Outer, Inner]#l, E] = {
    askInd[CurryT[Outer, Inner]#l, Inner, E](lift, under)
  }
}

private[instances] trait AskingInstancesLowPriority extends AskInstancesLowPriority1 {
  implicit final def askInd[M[_], Inner[_], E](implicit
                                               lift: applicative.Layer[M, Inner],
                                               under: Asking[Inner, E]
                                              ): Asking[M, E] = {
    new Asking[M, E] {
      val applicative = lift.outerInstance

      def ask: M[E] = lift.layer(under.ask)

      def reader[A](f: E => A): M[A] = lift.layer(under.reader(f))
    }
  }

}

private[instances] trait AskInstancesLowPriority1 {
  implicit final def askReader[M[_], E](implicit M: Applicative[M]): Asking[CurryT[ReaderTCE[E]#l, M]#l, E] = {
    new Asking[ReaderTC[M, E]#l, E] {
      val applicative = ReaderT.catsDataApplicativeForKleisli(M)

      def ask: ReaderT[M, E, E] = Kleisli.ask[M, E]

      def reader[A](f: E => A): ReaderT[M, E, A] = Kleisli(a => M.pure(f(a)))
    }
  }

  implicit final def askFunction[E]: Asking[FunctionC[E]#l, E] = {
    new Asking[FunctionC[E]#l, E] {
      val applicative = cats.instances.function.catsStdMonadReaderForFunction1

      def ask: E => E = identity[E]

      def reader[A](f: E => A): E => A = f
    }
  }
}

object asking extends AskingInstances

