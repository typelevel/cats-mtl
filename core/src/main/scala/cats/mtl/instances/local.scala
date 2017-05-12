package cats
package mtl
package instances

import cats.data.ReaderT
import cats.mtl.evidence.Nat

trait LocalInstances extends LocalLowPriorityInstances {

  implicit def localNReader[M[_], E](implicit M: Monad[M]): Local.Aux[Nat.Zero, CurryT[ReaderTC[E]#l, M]#l, E] =
    new Local[CurryT[ReaderTC[E]#l, M]#l, E] {
      val monad = M
      type N = Nat._0
      val ask: Ask.Aux[Nat.Zero, CurryT[ReaderTC[E]#l, M]#l, E] =
        ask.askReader[M, E]

      def local[A](fa: ReaderT[M, E, A])(f: (E) => E): ReaderT[M, E, A] =
        ReaderT.local(f)(fa)
    }

}

trait LocalLowPriorityInstances {

  implicit def localNInd[N0 <: Nat, T[_[_], _], M[_], E](implicit TM: Monad[CurryT[T, M]#l],
                                                         lift: TransLift.AuxId[T],
                                                         mf: AFunctor[T],
                                                         under: Local.Aux[N0, M, E]
                                                       ): Local.Aux[Nat.Succ[N0], CurryT[T, M]#l, E] =
    new Local[CurryT[T, M]#l, E] {
      val monad = TM
      type N = Nat.Succ[N0]
      val ask: Ask.Aux[Nat.Succ[N0], CurryT[T, M]#l, E] =
        ask.askInd[N0, T, M, E](TM, lift, under.ask)

      def local[A](fa: T[M, A])(f: (E) => E): T[M, A] =
        mf.hoist(fa)(new (M ~> M) {
          def apply[X](fa: M[X]): M[X] = under.local(fa)(f)
        })(under.ask.monad)
    }

}

object local extends LocalInstances
