package cats
package mtl
package instances

import cats.data.ReaderT
import cats.mtl.applicative.{Asking, Scoping}

trait ScopingInstances extends ScopingLowPriorityInstances1 {
  implicit final def scopingInd[M[_], Inner[_], E](implicit ml: monad.Layer[M, Inner],
                                                   under: Scoping[Inner, E]
                                                  ): Scoping[M, E] = {
    new Scoping[M, E] {
      val ask: Asking[M, E] =
        instances.asking.askInd[M, Inner, E](ml, under.ask)

      def local[A](fa: M[A])(f: E => E): M[A] = {
        ml.outerInstance.flatMap(ask.ask)(r =>
          ml.layerImapK(fa)(new (Inner ~> Inner) {
            def apply[X](fa: Inner[X]): Inner[X] = under.local(fa)(f)
          }, new (Inner ~> Inner) {
            def apply[X](fa: Inner[X]): Inner[X] = under.scope(fa)(r)
          }))
      }

      def scope[A](fa: M[A])(e: E): M[A] = {
        ml.outerInstance.flatMap(ask.ask)(r =>
          ml.layerImapK(fa)(new (Inner ~> Inner) {
            def apply[X](fa: Inner[X]): Inner[X] = under.scope(fa)(e)
          }, new (Inner ~> Inner) {
            def apply[X](fa: Inner[X]): Inner[X] = under.scope(fa)(r)
          }))
      }

    }
  }
}

private[instances] trait ScopingLowPriorityInstances1 {
  implicit final def scopingNReader[M[_], E](implicit M: Applicative[M]): Scoping[CurryT[ReaderTCE[E]#l, M]#l, E] = {
    new Scoping[ReaderTC[M, E]#l, E] {
      val ask: Asking[ReaderTC[M, E]#l, E] =
        instances.asking.askReader[M, E]

      def local[A](fa: ReaderT[M, E, A])(f: E => E): ReaderT[M, E, A] = ReaderT.local(f)(fa)

      def scope[A](fa: ReaderT[M, E, A])(e: E): ReaderT[M, E, A] = ReaderT(_ => fa.run(e))
    }
  }

  implicit final def scopingFunction[E]: Scoping[FunctionC[E]#l, E] = {
    new Scoping[FunctionC[E]#l, E] {
      val ask: Asking[FunctionC[E]#l, E] =
        instances.asking.askFunction[E]

      def local[A](fa: E => A)(f: E => E): E => A = fa.compose(f)

      def scope[A](fa: E => A)(e: E): E => A = _ => fa(e)
    }
  }
}

object scoping extends ScopingInstances
