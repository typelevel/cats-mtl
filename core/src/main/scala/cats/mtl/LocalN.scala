package cats
package mtl

import cats.data.ReaderT
import evidence.Nat

trait LocalN[N <: Nat, F[_], E] {
  val read: AskN[N, F, E]

  def local[A](fa: F[A])(f: E => E): F[A]

  def scope[A](e: E)(fa: F[A]): F[A] =
    local(fa)(_ => e)
}

object LocalN {

  implicit def localNReader[M[_], E](implicit M: Monad[M]): LocalN[Nat.Zero, CurryT[ReaderTC[E]#l, M]#l, E] =
    new LocalN[Nat.Zero, CurryT[ReaderTC[E]#l, M]#l, E] {
      val monad = M
      val read: AskN[Nat.Zero, CurryT[ReaderTC[E]#l, M]#l, E] =
        AskN.askNReader[M, E]

      def local[A](fa: ReaderT[M, E, A])(f: (E) => E): ReaderT[M, E, A] =
        ReaderT.local(f)(fa)
    }

  implicit def localNInd[N <: Nat, T[_[_], _], M[_], E](implicit TM: Monad[CurryT[T, M]#l],
                                                        lift: TransLift.AuxId[T],
                                                        mf: MMonad[T],
                                                        under: LocalN[N, M, E]
                                                       ): LocalN[Nat.Succ[N], CurryT[T, M]#l, E] =
    new LocalN[Nat.Succ[N], CurryT[T, M]#l, E] {
      val monad = TM
      val read: AskN[Nat.Succ[N], CurryT[T, M]#l, E] =
        AskN.askNInd[N, T, M, E](TM, lift, under.read)

      def local[A](fa: T[M, A])(f: (E) => E): T[M, A] =
        mf.hoist(fa)(new (M ~> M) {
          def apply[X](fa: M[X]): M[X] = under.local(fa)(f)
        })
    }

}
