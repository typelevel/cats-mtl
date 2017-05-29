package cats
package mtl
package instances

import cats.data.{ReaderT, StateT}
import cats.mtl.monad.{Asking, Layer}

trait AskingInstances extends AskingInstancesLowPriority1 {
  implicit def askIndT[Inner[_], Outer[_[_], _], E](implicit
                                                    lift: monad.Trans.AuxIO[CurryT[Outer, Inner]#l, Inner, Outer],
                                                    under: Asking[Inner, E]
                                                   ): Asking[CurryT[Outer, Inner]#l, E] =
    new Asking[CurryT[Outer, Inner]#l, E] {
      def ask: CurryT[Outer, Inner]#l[E] =
        lift.layer(under.ask)

      def reader[A](f: (E) => A): Outer[Inner, A] =
        lift.layer(under.reader(f))
    }

}

trait AskingInstancesLowPriority1 extends AskInstancesLowPriority2 {

  implicit def askInd[M[_], Inner[_], E](implicit
                                         lift: Layer[M, Inner],
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

object asking extends AskingInstances

