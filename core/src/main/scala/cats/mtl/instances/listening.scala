package cats
package mtl
package instances

import cats.data.WriterT
import cats.syntax.functor._

trait ListeningInstances extends ListenLowPriorityInstances {
  implicit final def localInd[M[_], Inner[_], L](implicit
                                                 lift: monad.Layer[M, Inner],
                                                 under: monad.Listening[Inner, L]
                                                ): monad.Listening[M, L] = {
    new monad.Listening[M, L] {
      val tell = instances.telling.tellInd[M, Inner, L](lift, under.tell)

      def listen[A](fa: M[A]): M[(A, L)] = {
        lift.outerMonad.flatMap(fa) { a =>
          lift.layer(under.listen(lift.innerMonad.pure(a)))
        }
      }

      def pass[A](fa: M[(A, L => L)]): M[A] = {
        lift.outerMonad.flatMap(fa) { a =>
          lift.layer(under.pass(lift.innerMonad.pure(a)))
        }
      }
    }
  }
}

trait ListenLowPriorityInstances {
  implicit final def localWriter[M[_], L](implicit M: Monad[M], L: Monoid[L]): monad.Listening[WriterTC[M, L]#l, L] = {
    new monad.Listening[WriterTC[M, L]#l, L] {
      val tell = instances.telling.tellWriter[M, L]

      def listen[A](fa: WriterT[M, L, A]): WriterT[M, L, (A, L)] = {
        WriterT[M, L, (A, L)](fa.run.map { case (l, a) => (l, (a, l)) })
      }

      def pass[A](fa: WriterT[M, L, (A, L => L)]): WriterT[M, L, A] = {
        WriterT[M, L, A](fa.run.map { case (l, (a, f)) => (f(l), a) })
      }
    }
  }

  implicit final def localTuple[L](implicit L: Monoid[L]): monad.Listening[TupleC[L]#l, L] = {
    new monad.Listening[TupleC[L]#l, L] {
      val tell = instances.telling.tellTuple[L]

      def listen[A](fa: (L, A)): (L, (A, L)) = {
        val (l, a) = fa
        (l, (a, l))
      }

      def pass[A](fa: (L, (A, L => L))): (L, A) = {
        val (l, (a, f)) = fa
        (f(l), a)
      }
    }
  }
}

object listening {

}
