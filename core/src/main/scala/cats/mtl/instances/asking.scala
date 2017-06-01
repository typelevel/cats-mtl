package cats
package mtl
package instances

import cats.data.{ReaderT, StateT}

trait AskingInstances extends AskingInstancesLowPriority1 {
  implicit def askIndT[Inner[_], Outer[_[_], _], E]
  (implicit lift: monad.Trans.Aux[CurryT[Outer, Inner]#l, Inner, Outer],
   under: monad.Asking[Inner, E]): monad.Asking[CurryT[Outer, Inner]#l, E] =
    askInd[CurryT[Outer, Inner]#l, Inner, E](lift, under)

}

trait AskingInstancesLowPriority1 extends AskInstancesLowPriority2 {

  implicit def askInd[M[_], Inner[_], E](implicit
                                         lift: monad.Layer[M, Inner],
                                         under: monad.Asking[Inner, E]
                                        ): monad.Asking[M, E] =
    new monad.Asking[M, E] {
      def ask: M[E] =
        lift.layer(under.ask)

      def reader[A](f: (E) => A): M[A] =
        lift.layer(under.reader(f))
    }

}

trait AskInstancesLowPriority2 extends AskInstancesLowPriority {

  implicit def askReader[M[_], E](implicit M: Applicative[M]): monad.Asking[CurryT[ReaderTCE[E]#l, M]#l, E] =
    new monad.Asking[CurryT[ReaderTCE[E]#l, M]#l, E] {
      def ask: ReaderT[M, E, E] =
        ReaderT.ask[M, E]
      def reader[A](f: (E) => A): ReaderT[M, E, A] =
        ReaderT(a => M.pure(f(a)))
    }

}

trait AskInstancesLowPriority {

  implicit def askState[M[_], S](implicit M: Applicative[M]): monad.Asking[CurryT[StateTCS[S]#l, M]#l, S] =
    new monad.Asking[CurryT[StateTCS[S]#l, M]#l, S] {
      def ask: StateT[M, S, S] =
        StateT.get[M, S]
      def reader[A](f: (S) => A): StateT[M, S, A] =
        StateT(s => M.pure((s, f(s))))
    }

}

object asking extends AskingInstances

