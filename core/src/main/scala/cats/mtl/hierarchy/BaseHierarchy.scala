package cats
package mtl
package hierarchy

import cats.mtl.monad.Aborting

object BaseHierarchy {

  trait BH0 extends BH1 {
    implicit final def askFromLocal[F[_], E](local: monad.Scoping[F, E]): monad.Asking[F, E] = local.ask

    implicit final def tellFromListen[F[_], L](listen: monad.Listening[F, L]): monad.Telling[F, L] = listen.tell

    implicit final def raiseFromHandle[F[_], E](handle: monad.Handling[F, E]): monad.Raising[F, E] = handle.raise

    implicit final def raiseFromAborting[F[_]](abort: Aborting[F]): monad.Raising[F, Unit] = {
      new monad.Raising[F, Unit] {
        val monad: Monad[F] = abort.monad

        def raise[A](e: Unit): F[A] = abort.abort[A]
      }
    }

  }

  trait BH1 {

  }

}

