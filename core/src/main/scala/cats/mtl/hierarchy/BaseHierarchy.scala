package cats
package mtl
package hierarchy

object BaseHierarchy {

  trait BH0 extends BH1 {
    implicit final def askFromLocal[F[_], E](local: monad.Scoping[F, E]): monad.Asking[F, E] = local.ask

  }

  trait BH1 {

  }
}

