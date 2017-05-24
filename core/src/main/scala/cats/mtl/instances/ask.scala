package cats
package mtl
package instances

import cats.data.{ReaderT, StateT}

trait AskInstances extends AskInstancesLowPriority1 {

  implicit def askIndT[Inner[_], Outer[_[_], _], E](implicit
                                                    lift: MonadTrans.AuxIO[CurryT[Outer, Inner]#l, Inner, Outer],
                                                    under: Asking[Inner, E]
                                                   ): Asking[CurryT[Outer, Inner]#l, E] =
    new Asking[CurryT[Outer, Inner]#l, E] {
      def ask: CurryT[Outer, Inner]#l[E] =
        lift.layer(under.ask)

      def reader[A](f: (E) => A): Outer[Inner, A] =
        lift.layer(under.reader(f))
    }

}

trait AskInstancesLowPriority1 extends AskInstancesLowPriority2 {

  implicit def askInd[M[_], Inner[_], E](implicit
                                         lift: MonadLayer[M, Inner],
                                         under: Asking[Inner, E]
                                        ): Asking[M, E] =
    new Asking[M, E] {
      def ask: M[E] =
        lift.layer(under.ask)

      def reader[A](f: (E) => A): M[A] =
        lift.layer(under.reader(f))
    }

}

trait AskInstancesLowPriority2 extends AskInstancesLowPriority {

  implicit def askReader[M[_], E](implicit M: Applicative[M]): Asking[CurryT[ReaderTCE[E]#l, M]#l, E] =
    new Asking[CurryT[ReaderTCE[E]#l, M]#l, E] {
      def ask: ReaderT[M, E, E] =
        ReaderT.ask[M, E]
      def reader[A](f: (E) => A): ReaderT[M, E, A] =
        ReaderT(a => M.pure(f(a)))
    }

}

trait AskInstancesLowPriority {

  implicit def askState[M[_], S](implicit M: Applicative[M]): Asking[CurryT[StateTCS[S]#l, M]#l, S] =
    new Asking[CurryT[StateTCS[S]#l, M]#l, S] {
      def ask: StateT[M, S, S] =
        StateT.get[M, S]
      def reader[A](f: (S) => A): StateT[M, S, A] =
        StateT(s => M.pure((s, f(s))))
    }

}

object ask extends AskInstances

