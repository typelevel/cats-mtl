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

final class SummonableImplicits extends BaseSuite {

  // test instances.all._
  //noinspection ScalaUnusedSymbol
  locally {
    val AskTest: Unit = {
      def _1 = Ask[ReaderStrId, String]
      def _2 = Ask[ReaderStrInt, Int]
      def _3 = Ask[ReaderStrInt, String]
    }

    val ListenTest: Unit = {
      def _1 = Listen[WriterStrId, String]
      def _2 = Listen[WriterTStrWriterTInt, String]
      def _3 = Listen[WriterTStrWriterTInt, Vector[Int]]
    }

    val RaiseTest: Unit = {
      def _1 = Raise[EitherStrId, String]
      //def _2 = Raise[EitherTStrEitherTInt, Int]
      def _3 = Raise[EitherTStrEitherTInt, String]
    }

    val LocalTest: Unit = {
      def _1 = Local[ReaderStrId, String]
      def _2 = Local[ReaderStrInt, Int]
      def _3 = Local[ReaderStrInt, String]
    }

    val StatefulTest: Unit = {
      def _1 = Stateful[StateStrId, String]
      def _2 = Stateful[StateTStrStateTInt, String]
      def _3 = Stateful[StateTStrStateTInt, Int]
    }

    val TellTest: Unit = {
      def _1 = Tell[WriterStrId, String]
      def _2 = Tell[WriterTStrWriterTInt, String]
      def _3 = Tell[WriterTStrWriterTInt, Vector[Int]]
    }
  }

  // test hierarchy.base
  //noinspection ScalaUnusedSymbol
  locally {
    def localToAsk[F[_]](implicit F: Local[F, String]): Ask[F, String] =
      Ask[F, String]

    def listenToTell[F[_]](implicit F: Listen[F, String]): Tell[F, String] =
      Tell[F, String]

    def stateToTell[F[_]](implicit F: Tell[F, String]): Tell[F, String] =
      Tell[F, String]

    def stateToAsk[F[_]](implicit F: Ask[F, String]): Ask[F, String] =
      Ask[F, String]

  }

}
