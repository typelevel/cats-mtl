package cats
package mtl
package instances

import cats.data._
import cats.syntax.all._

trait BracketingInstances extends BracketingInstancesLowPriority {
  implicit final def handleNEither[M[_], E]
  (implicit M: monad.Bracketing[M]): monad.Bracketing[EitherTC[M, E]#l] = {
    new monad.Bracketing[EitherTC[M, E]#l] {
      val monad = EitherT.catsDataMonadErrorForEitherT(M.monad)

      def bracket[A, B, C](action: EitherT[M, E, A])
                          (bind: (A) => EitherT[M, E, B], cleanup: (A) => EitherT[M, E, C]): EitherT[M, E, B] = {
        EitherT[M, E, B](M.monad.flatMap(action.value) {
          case Left(l) => M.monad.pure(Either.left[E, B](l))
          case Right(a) => M.bracket(M.monad.pure(a))(bind(_).value, _ => cleanup(a).value)
        })
      }
    }
  }
}

trait BracketingInstancesLowPriority {
  implicit final def handleNIndState[M[_], S, Err]
  (implicit M: monad.Bracketing[M]): monad.Bracketing[CurryT[StateTCS[S]#l, M]#l] = {
    new monad.Bracketing[CurryT[StateTCS[S]#l, M]#l] {
      val monad = StateT.catsDataMonadForStateT(M.monad)

      def bracket[A, B, C](action: StateT[M, S, A])
                          (bind: (A) => StateT[M, S, B], cleanup: (A) => StateT[M, S, C]): StateT[M, S, B] = {
        ???
      }
    }
  }

  implicit final def handleNIndReader[M[_], Env, Err]
  (implicit M: monad.Bracketing[M]): monad.Bracketing[CurryT[ReaderTCE[Env]#l, M]#l] = {
    new monad.Bracketing[CurryT[ReaderTCE[Env]#l, M]#l] {
      val monad = ReaderT.catsDataMonadReaderForKleisli(M.monad)

      def bracket[A, B, C](action: ReaderT[M, Env, A])
                          (bind: (A) => ReaderT[M, Env, B], cleanup: (A) => ReaderT[M, Env, C]): ReaderT[M, Env, B] = {
        ???
      }
    }
  }

  implicit final def handleNIndWriter[M[_], L, Err]
  (implicit L: Monoid[L],
   M: monad.Bracketing[M]): monad.Bracketing[CurryT[WriterTCL[L]#l, M]#l] = {
    new monad.Bracketing[CurryT[WriterTCL[L]#l, M]#l] {
      val monad = WriterT.catsDataMonadWriterForWriterT(M.monad, L)

      def bracket[A, B, C](action: WriterT[M, L, A])
                          (bind: (A) => WriterT[M, L, B], cleanup: (A) => WriterT[M, L, C]): WriterT[M, L, B] = {
        WriterT[M, L, B](M.monad.flatMap(action.run) {
          case (l, a) => M.monad.map(M.bracket(M.monad.pure(a))(bind(_).run, _ => cleanup(a).run)) {
            case (l2, b) => (L.combine(l, l2), b)
          }
        })
      }
    }
  }
}

object Bracketing extends BracketingInstances
