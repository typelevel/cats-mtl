package cats
package mtl
package instances

import cats.data.{Kleisli, ReaderT}
import cats.mtl.ApplicativeAsk

trait AskInstances extends AskInstancesLowPriority1 {
  implicit final def askLayerInd[M[_], Inner[_], E](implicit
                                                    lift: ApplicativeLayer[M, Inner],
                                                    under: ApplicativeAsk[Inner, E]
                                                   ): ApplicativeAsk[M, E] = {
    new ApplicativeAsk[M, E] {
      val applicative = lift.outerInstance

      def ask: M[E] = lift.layer(under.ask)

      def reader[A](f: E => A): M[A] = lift.layer(under.reader(f))
    }
  }

}

private[instances] trait AskInstancesLowPriority1 {
  implicit final def askReader[M[_], E](implicit M: Applicative[M]): ApplicativeAsk[CurryT[ReaderTCE[E]#l, M]#l, E] = {
    new ApplicativeAsk[ReaderTC[M, E]#l, E] {
      val applicative: Applicative[ReaderTC[M, E]#l] =
        ReaderT.catsDataApplicativeForKleisli(M)

      def ask: ReaderT[M, E, E] = Kleisli.ask[M, E]

      def reader[A](f: E => A): ReaderT[M, E, A] = Kleisli(a => M.pure(f(a)))
    }
  }

  implicit final def askFunction[E]: ApplicativeAsk[FunctionC[E]#l, E] = {
    new ApplicativeAsk[FunctionC[E]#l, E] {
      val applicative: Applicative[FunctionC[E]#l] =
        cats.instances.function.catsStdMonadReaderForFunction1

      def ask: E => E = identity[E]

      def reader[A](f: E => A): E => A = f
    }
  }
}

object ask extends AskInstances

