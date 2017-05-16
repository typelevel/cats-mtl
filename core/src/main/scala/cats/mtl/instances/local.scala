package cats
package mtl
package instances

// import cats.data.ReaderT

trait LocalInstances extends LocalLowPriorityInstances {
/*
  implicit def localNReader[M[_], E](implicit M: Monad[M]): Local[CurryT[ReaderTCE[E]#l, M]#l, E] =
    new Local[CurryT[ReaderTCE[E]#l, M]#l, E] {
      val monad = M
      val ask: Ask[CurryT[ReaderTCE[E]#l, M]#l, E] =
        instances.ask.askReader[M, E]

      def local[A](fa: ReaderT[M, E, A])(f: (E) => E): ReaderT[M, E, A] =
        ReaderT.local(f)(fa)
    }
 */
}

trait LocalLowPriorityInstances {
/*
  implicit def localNInd[T[_[_], _], M[_], E](implicit TM: Monad[CurryT[T, M]#l],
                                                         lift: TransLift.AuxId[T],
                                                         mf: AFunctor[T],
                                                         under: Local[M, E]
                                                       ): Local[CurryT[T, M]#l, E] =
    new Local[CurryT[T, M]#l, E] {
      val monad = TM
      val ask: Ask[CurryT[T, M]#l, E] =
        instances.ask.askInd[T, M, E](TM, lift, under.ask)

      def local[A](fa: T[M, A])(f: (E) => E): T[M, A] =
        mf.hoist(fa)(new (M ~> M) {
          def apply[X](fa: M[X]): M[X] = under.local(fa)(f)
        })(under.ask.monad)
    }
*/
}

object local extends LocalInstances
