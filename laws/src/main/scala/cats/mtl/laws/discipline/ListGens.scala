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
package mtl.laws.discipline

import org.scalacheck.Gen

private[discipline] object ListGens {
  private[this] val smallPositiveInt: Gen[Int] = Gen.oneOf(1 to 5)

  private[this] def genListDrop: Gen[List ~> List] =
    for (count <- smallPositiveInt)
      yield new (List ~> List) {
        def apply[A](list: List[A]): List[A] = list.drop(count)
        override def toString: String = s"List#drop($count)"
      }

  private[this] def genListTake: Gen[List ~> List] =
    for (count <- smallPositiveInt)
      yield new (List ~> List) {
        def apply[A](list: List[A]): List[A] = list.take(count)
        override def toString: String = s"List#take($count)"
      }

  private[this] def genListSlice: Gen[List ~> List] =
    for {
      start <- smallPositiveInt
      offset <- smallPositiveInt
    } yield new (List ~> List) {
      def apply[A](list: List[A]): List[A] =
        list.slice(start, start + offset)
      override def toString: String = s"List#slice($start, $offset)"
    }

  private[this] def genListFilterEvenOrOddIndices: Gen[List ~> List] =
    for (remainder <- Gen.oneOf(0, 1))
      yield new (List ~> List) {
        def apply[A](list: List[A]): List[A] =
          list.view.zipWithIndex.filter(_._2 % 2 == remainder).map(_._1).toList
        override def toString: String =
          s"List#filter(<index is ${if (remainder == 0) "even" else "odd"}>)"
      }

  val genFunctionKListList: Gen[List ~> List] =
    Gen.oneOf(
      genListDrop,
      genListTake,
      genListSlice,
      genListFilterEvenOrOddIndices
    )
}
