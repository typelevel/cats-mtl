/*
 * Copyright 2021 Typelevel
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

import cats.data.{ReaderWriterStateT => RWST, _}

final class SummonableImplicits extends BaseSuite {

  locally {
    {
      Ask[ReaderStrId, String]
      Ask[ReaderStrInt, Int]
      Ask[ReaderStrInt, String]
    }

    {
      LiftValue[Id, Id]
      LiftValue[Id, OptionTId]
      LiftValue[Id, EitherTStrId]
      LiftValue[Id, EitherTIntId]
      LiftValue[Id, IorTStrId]
      LiftValue[Id, IorTIntId]
      LiftValue[Id, KleisliStrId]
      LiftValue[Id, KleisliIntId]
      LiftValue[Id, WriterTStrId]
      LiftValue[Id, WriterTIntId]
      LiftValue[Id, StateTStrId]
      LiftValue[Id, StateTIntId]
      LiftValue[Id, RWST[Id, Int, Int, Int, *]]
      LiftValue[Id, RWST[Id, String, String, String, *]]
      LiftValue[Id, OptionT[IorT[Id, Int, *], *]]
    }

    {
      LiftKind[Id, Id]
      LiftKind[Id, OptionTId]
      LiftKind[Id, EitherTStrId]
      LiftKind[Id, EitherTIntId]
      LiftKind[Id, IorTStrId]
      LiftKind[Id, IorTIntId]
      LiftKind[Id, KleisliStrId]
      LiftKind[Id, KleisliIntId]
      LiftKind[Id, WriterTStrId]
      LiftKind[Id, WriterTIntId]
      LiftKind[Id, OptionT[IorT[Id, Int, *], *]]
    }

    {
      Listen[WriterTStrId, String]
      Listen[WriterTStrWriterTInt, String]
      Listen[WriterTStrWriterTInt, Vector[Int]]
    }

    {
      Local[ReaderStrId, String]
      Local[ReaderStrInt, Int]
      Local[ReaderStrInt, String]
    }

    {
      Raise[EitherTStrId, String]
      // Raise[EitherTStrEitherTInt, Int]
      Raise[EitherTStrEitherTInt, String]
    }

    {
      Stateful[StateTStrId, String]
      Stateful[StateTStrStateTInt, String]
      Stateful[StateTStrStateTInt, Int]
    }

    {
      Tell[WriterTStrId, String]
      Tell[WriterTStrWriterTInt, String]
      Tell[WriterTStrWriterTInt, Vector[Int]]
    }
  }
}
