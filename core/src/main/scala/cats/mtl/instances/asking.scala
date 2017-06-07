package cats
package mtl
package instances

import cats.data.{Kleisli, ReaderT}

trait AskingInstances extends AskingInstancesLowPriority {
  implicit final def askIndT[Inner[_], Outer[_[_], _], E]
  (implicit lift: monad.Trans.Aux[CurryT[Outer, Inner]#l, Inner, Outer],
   under: monad.Asking[Inner, E]): monad.Asking[CurryT[Outer, Inner]#l, E] = {
    askInd[CurryT[Outer, Inner]#l, Inner, E](lift, under)
  }
}

private[instances] trait AskingInstancesLowPriority extends AskInstancesLowPriority1 {
  implicit final def askInd[M[_], Inner[_], E](implicit
                                               lift: monad.Layer[M, Inner],
                                               under: monad.Asking[Inner, E]
                                              ): monad.Asking[M, E] = {
    new monad.Asking[M, E] {
      val monad = lift.outerMonad

      def ask: M[E] = lift.layer(under.ask)

      def reader[A](f: E => A): M[A] = lift.layer(under.reader(f))
    }
  }

}

private[instances] trait AskInstancesLowPriority1 {
  implicit final def askReader[M[_], E](implicit M: Monad[M]): monad.Asking[CurryT[ReaderTCE[E]#l, M]#l, E] = {
    new monad.Asking[ReaderTC[M, E]#l, E] {
      val monad = ReaderT.catsDataMonadReaderForKleisli(M)

      def ask: ReaderT[M, E, E] = Kleisli.ask[M, E]

      def reader[A](f: E => A): ReaderT[M, E, A] = Kleisli(a => M.pure(f(a)))
    }
  }

  implicit final def askFunction[E]: monad.Asking[FunctionC[E]#l, E] = {
    new monad.Asking[FunctionC[E]#l, E] {
      val monad = cats.instances.function.catsStdMonadReaderForFunction1

      def ask: (E) => E = identity[E]

      def reader[A](f: (E) => A): (E) => A = f
    }
  }
}

object asking extends AskingInstances

