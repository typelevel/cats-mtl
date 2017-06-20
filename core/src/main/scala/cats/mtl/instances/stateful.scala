package cats
package mtl
package instances

import data.StateT

trait StatefulInstances extends StatefulInstancesLowPriority {
  implicit final def statefulNIndT[T[_[_], _], M[_], E]
  (implicit lift: monad.TransFunctor.Aux[CurryT[T, M]#l, M, T],
   under: monad.Stateful[M, E]): monad.Stateful[CurryT[T, M]#l, E] = {
    statefulNInd[CurryT[T, M]#l, M, E](lift, under)
  }

}

private[instances] trait StatefulInstancesLowPriority extends StatefulInstancesLowPriority1 {
  // this dependency on LayerFunctor is required because non-`LayerFunctor`s may not be lawful
  // to lift Stateful into
  implicit final def statefulNInd[M[_], Inner[_], E](implicit ml: monad.LayerFunctor[M, Inner],
                                                     under: monad.Stateful[Inner, E]
                                                    ): monad.Stateful[M, E] = {
    new monad.Stateful[M, E] {
      val fMonad: Monad[M] = ml.outerInstance

      def get: M[E] = ml.layer(under.get)

      def set(s: E): M[Unit] = ml.layer(under.set(s))

      def modify(f: E => E): M[Unit] = ml.layer(under.modify(f))
    }
  }
}

private[instances] trait StatefulInstancesLowPriority1 {
  implicit final def statefulState[M[_], S](implicit M: Monad[M]): monad.Stateful[CurryT[StateTCS[S]#l, M]#l, S] = {
    new monad.Stateful[StateTC[M, S]#l, S] {
      val fMonad: Monad[StateTC[M, S]#l] = StateT.catsDataMonadForStateT

      def get: StateT[M, S, S] = StateT.get

      def set(s: S): StateT[M, S, Unit] = StateT.set(s)

      def modify(f: S => S): StateT[M, S, Unit] = StateT.modify(f)
    }
  }
}

object stateful extends StatefulInstances
