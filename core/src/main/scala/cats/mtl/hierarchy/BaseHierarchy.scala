package cats
package mtl
package hierarchy

import cats.mtl.applicative.{Asking, Scoping, Telling}
import cats.mtl.functor.{Aborting, Raising}

object BaseHierarchy {

  trait BH0 extends BH1 {
    implicit final def askFromLocal[F[_], E](local: Scoping[F, E]): Asking[F, E] = local.ask

    implicit final def tellFromListen[F[_], L](listen: monad.Listening[F, L]): Telling[F, L] = listen.tell

    implicit final def raiseFromHandle[F[_], E](handle: monad.Handling[F, E]): Raising[F, E] = handle.raise

    implicit final def raiseFromAborting[F[_]](abort: Aborting[F]): Raising[F, Unit] = {
      new Raising[F, Unit] {
        val functor: Functor[F] = abort.functor

        def raise[A](e: Unit): F[A] = abort.abort[A]
      }
    }

  }

  trait BH1 {

  }

}

