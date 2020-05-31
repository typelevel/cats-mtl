/*
 * Copyright 2020 Typelevel
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cats
package mtl

import cats.data._

/**
  * Encapsulates the notion of a monad, G, which contains all of
  * the effects of some other monad, F. This means that any effect
  * of type F[A] can be lifted to G[A], such that both F and G
  * form monads and the lifting distributes over flatMap and pure.
  *
 * Original idea by Kris Nuttycombe.
  */
trait MonadPartialOrder[F[_], G[_]] extends (F ~> G) {
  def monadF: Monad[F]
  def monadG: Monad[G]
}

private[mtl] trait MonadPartialOrderInstances {

  implicit def monadPartialOrderForStateT[F[_], S](
      implicit F: Monad[F]): MonadPartialOrder[F, StateT[F, S, *]] =
    new MonadPartialOrder[F, StateT[F, S, *]] {
      val monadF = F
      val monadG = IndexedStateT.catsDataMonadForIndexedStateT[F, S]
      def apply[A](fa: F[A]) = StateT.liftF(fa)
    }

  implicit def monadPartialOrderForKleisli[F[_], R](
      implicit F: Monad[F]): MonadPartialOrder[F, Kleisli[F, R, *]] =
    new MonadPartialOrder[F, Kleisli[F, R, *]] {
      val monadF = F
      val monadG = Kleisli.catsDataMonadForKleisli[F, R]
      def apply[A](fa: F[A]) = Kleisli.liftF(fa)
    }

  implicit def monadPartialOrderForEitherT[F[_], E](
      implicit F: Monad[F]): MonadPartialOrder[F, EitherT[F, E, *]] =
    new MonadPartialOrder[F, EitherT[F, E, *]] {
      val monadF = F
      val monadG = EitherT.catsDataMonadErrorForEitherT[F, E]
      def apply[A](fa: F[A]) = EitherT.liftF(fa)
    }

  implicit def monadPartialOrderForWriterT[F[_], L: Monoid](
      implicit F: Monad[F]): MonadPartialOrder[F, WriterT[F, L, *]] =
    new MonadPartialOrder[F, WriterT[F, L, *]] {
      val monadF = F
      val monadG = WriterT.catsDataMonadForWriterT[F, L]
      def apply[A](fa: F[A]) = WriterT.liftF(fa)
    }

  implicit def monadPartialOrderForOptionT[F[_]](
      implicit F: Monad[F]): MonadPartialOrder[F, OptionT[F, *]] =
    new MonadPartialOrder[F, OptionT[F, *]] {
      val monadF = F
      val monadG = OptionT.catsDataMonadForOptionT[F]
      def apply[A](fa: F[A]) = OptionT.liftF(fa)
    }

  implicit def monadPartialOrderForRWST[F[_], E, L: Monoid, S](
      implicit F: Monad[F]): MonadPartialOrder[F, RWST[F, E, L, S, *]] =
    new MonadPartialOrder[F, RWST[F, E, L, S, *]] {
      val monadF = F
      val monadG = IndexedReaderWriterStateT.catsDataMonadForRWST[F, E, L, S]
      def apply[A](fa: F[A]) = RWST.liftF(fa)
    }

  implicit def monadPartialOrderForIorT[F[_], E: Semigroup](
      implicit F: Monad[F]): MonadPartialOrder[F, IorT[F, E, *]] =
    new MonadPartialOrder[F, IorT[F, E, *]] {
      def apply[A](fa: F[A]): IorT[F, E, A] = IorT.liftF(fa)
      val monadF: Monad[F] = F
      val monadG: Monad[IorT[F, E, *]] = IorT.catsDataMonadErrorForIorT[F, E]
    }
}

object MonadPartialOrder extends MonadPartialOrderInstances {
  def apply[F[_], G[_]](implicit mpo: MonadPartialOrder[F, G]): MonadPartialOrder[F, G] = mpo
}
