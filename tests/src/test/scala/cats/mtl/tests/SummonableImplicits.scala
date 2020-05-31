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
package tests

import cats.implicits._

final class SummonableImplicits extends BaseSuite {

  // test instances.all._
  //noinspection ScalaUnusedSymbol
  locally {
    val ApplicativeAskTest: Unit = {
      def _1 = ApplicativeAsk[ReaderStrId, String]
      def _2 = ApplicativeAsk[ReaderStrInt, Int]
      def _3 = ApplicativeAsk[ReaderStrInt, String]
    }

    val FunctorListenTest: Unit = {
      def _1 = FunctorListen[WriterStrId, String]
      def _2 = FunctorListen[WriterTStrWriterTInt, String]
      def _3 = FunctorListen[WriterTStrWriterTInt, Vector[Int]]
    }

    val FunctorRaiseTest: Unit = {
      def _1 = FunctorRaise[EitherStrId, String]
      //def _2 = FunctorRaise[EitherTStrEitherTInt, Int]
      def _3 = FunctorRaise[EitherTStrEitherTInt, String]
    }

    val ApplicativeLocalTest: Unit = {
      def _1 = ApplicativeLocal[ReaderStrId, String]
      def _2 = ApplicativeLocal[ReaderStrInt, Int]
      def _3 = ApplicativeLocal[ReaderStrInt, String]
    }

    val MonadStateTest: Unit = {
      def _1 = MonadState[StateStrId, String]
      def _2 = MonadState[StateTStrStateTInt, String]
      def _3 = MonadState[StateTStrStateTInt, Int]
    }

    val FunctorTellTest: Unit = {
      def _1 = FunctorTell[WriterStrId, String]
      def _2 = FunctorTell[WriterTStrWriterTInt, String]
      def _3 = FunctorTell[WriterTStrWriterTInt, Vector[Int]]
    }
  }

  // test hierarchy.base
  //noinspection ScalaUnusedSymbol
  locally {
    def localToAsk[F[_]](implicit F: ApplicativeLocal[F, String]): ApplicativeAsk[F, String] =
      ApplicativeAsk[F, String]

    def listenToTell[F[_]](implicit F: FunctorListen[F, String]): FunctorTell[F, String] =
      FunctorTell[F, String]

    def stateToTell[F[_]](implicit F: FunctorTell[F, String]): FunctorTell[F, String] =
      FunctorTell[F, String]

    def stateToAsk[F[_]](implicit F: ApplicativeAsk[F, String]): ApplicativeAsk[F, String] =
      ApplicativeAsk[F, String]

  }

}
