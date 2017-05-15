package cats
package mtl
package hierarchy

object BaseHierarchy {

  trait BH0 extends BH1 {
    implicit def monadFromAsk[F[_], E](ask: Ask[F, E]): Monad[F] = ask.monad

    implicit def monadFromLocal[F[_], E](local: Local[F, E]): Monad[F] = local.ask.monad

    implicit def monadFromRaise[F[_], E](raise: Raise[F, E]): Monad[F] = raise.monad

    implicit def monadFromHandle[F[_], E](handle: Handle[F, E]): Monad[F] = handle.raise.monad

  }

  trait BH1 {
    implicit def askFromLocal[F[_], E](local: Local[F, E]): Ask[F, E] = local.ask

  }

}

