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

import cats.arrow.FunctionK
import cats.data._
import cats.laws.discipline.ExhaustiveCheck
import cats.syntax.{EqOps, EqSyntax}
import org.scalacheck.{Arbitrary, Gen}

import munit.{DisciplineSuite, FunSuite}

abstract class BaseSuite extends FunSuite with EqSyntax with DisciplineSuite {

  implicit def catsMtlLawsExhaustiveCheckForArbitrary[A: Arbitrary]: ExhaustiveCheck[A] =
    ExhaustiveCheck.instance(Gen.resize(30, Arbitrary.arbitrary[List[A]]).sample.get)

  protected type ReaderStr[M[_], A] = ReaderT[M, String, A]
  protected type ReaderStrId[A] = ReaderT[Id, String, A]
  protected type ReaderInt[M[_], A] = ReaderT[M, Int, A]
  protected type ReaderIntId[A] = Reader[Int, A]
  protected type ReaderStrInt[A] = ReaderStr[ReaderIntId, A]
  protected type ReaderStrFuncInt[A] = ReaderStr[FunctionC[Int]#l, A]

  protected type EitherTStr[M[_], A] = EitherT[M, String, A]
  protected type EitherStrId[A] = EitherT[Id, String, A]
  protected type EitherTInt[M[_], A] = EitherT[M, Int, A]
  protected type EitherTIntId[A] = EitherT[Id, Int, A]
  protected type EitherTStrEitherTInt[A] = EitherTStr[EitherTIntId, A]

  protected type StateTStr[M[_], A] = StateT[M, String, A]
  protected type StateStrId[A] = StateT[Id, String, A]
  protected type StateTInt[M[_], A] = StateT[M, Int, A]
  protected type StateTIntId[A] = StateT[Id, Int, A]
  protected type StateTStrStateTInt[A] = StateTStr[StateTIntId, A]

  protected type WriterTStr[M[_], A] = WriterT[M, String, A]
  protected type WriterStrId[A] = WriterT[Id, String, A]
  protected type WriterTInt[M[_], A] = WriterT[M, Vector[Int], A]
  protected type WriterTIntId[A] = WriterT[Id, Vector[Int], A]
  protected type WriterTStrWriterTInt[A] = WriterTStr[WriterTIntId, A]
  protected type WriterTStrTupleInt[A] = WriterTStr[TupleC[Vector[Int]]#l, A]
  protected type ReaderTStringOverWriterTStringOverOption[A] =
    ReaderT[WriterTC[Option, String]#l, List[Int], A]
  protected type StateTStringOverWriterTStringOverOption[A] =
    StateT[WriterTC[Option, String]#l, List[Int], A]
  protected type ReaderTIntOverReaderTStringOverOption[A] =
    ReaderT[ReaderTC[Option, String]#l, Int, A]
  protected type WriterTIntOverReaderTStringOverOption[A] =
    WriterT[ReaderTC[Option, String]#l, Int, A]
  protected type OptionTOverReaderTStringOverOption[A] = OptionT[ReaderTC[Option, String]#l, A]
  protected type EitherTIntOverReaderTStringOverOption[A] =
    EitherT[ReaderTC[Option, String]#l, Int, A]
  protected type StateTIntOverReaderTStringOverOption[A] =
    StateT[ReaderTC[Option, String]#l, Int, A]
  protected type ReaderTIntOverStateTStringOverOption[A] =
    ReaderT[StateTC[Option, String]#l, Int, A]
  protected type WriterTIntOverStateTStringOverOption[A] =
    WriterT[StateTC[Option, String]#l, Int, A]
  protected type OptionTOverStateTStringOverOption[A] = OptionT[StateTC[Option, String]#l, A]
  protected type EitherTIntOverStateTStringOverOption[A] =
    EitherT[StateTC[Option, String]#l, Int, A]
  protected type StateTIntOverStateTStringOverOption[A] =
    StateT[StateTC[Option, String]#l, Int, A]

  // disable Eq syntax (by making `catsSyntaxEq` not implicit), since it collides
  // with scalactic's equality
  override def catsSyntaxEq[A: Eq](a: A): EqOps[A] =
    new EqOps[A](a)

  implicit def tupleCTransArb[L](
      implicit arb: Arbitrary[L => L]): Arbitrary[TupleC[L]#l ~> TupleC[L]#l] = {
    Arbitrary(arb.arbitrary.map { f =>
      new (TupleC[L]#l ~> TupleC[L]#l) {
        def apply[A](fa: (L, A)): (L, A) = (f(fa._1), fa._2)
      }
    })
  }

  implicit def idTransArb: Arbitrary[Id ~> Id] = Arbitrary(Gen.const(FunctionK.id[Id]))

  def tweakableCatsLawsEqForFn1[A, B](
      cnt: Int)(implicit A: Arbitrary[A], B: Eq[B]): Eq[A => B] =
    new Eq[A => B] {
      def eqv(f: A => B, g: A => B): Boolean = {
        val samples = List.fill(cnt)(A.arbitrary.sample).collect {
          case Some(a) => a
          case None => sys.error("Could not generate arbitrary values to compare two functions")
        }
        samples.forall(s => B.eqv(f(s), g(s)))
      }
    }

  implicit def eitherTransArb[E](
      implicit arbF: Arbitrary[E => E],
      arbE: Arbitrary[E]): Arbitrary[EitherC[E]#l ~> EitherC[E]#l] = {
    Arbitrary(for {
      num <- Gen.chooseNum(1, 3)
      res <-
        if (num == 1) Gen.const(FunctionK.id[EitherC[E]#l])
        else if (num == 2)
          arbF
            .arbitrary
            .map(f =>
              new (EitherC[E]#l ~> EitherC[E]#l) {
                def apply[A](e: E Either A) = e.left.map(f)
              })
        else
          arbE
            .arbitrary
            .map(e =>
              new (EitherC[E]#l ~> EitherC[E]#l) {
                def apply[A](unused: E Either A) = Left(e)
              })
    } yield res)
  }

  implicit def optionTransArb: Arbitrary[Option ~> Option] = {
    Arbitrary {
      Arbitrary.arbBool.arbitrary.map { no =>
        if (no) new (Option ~> Option) {
          def apply[A](o: Option[A]) = None
        }
        else FunctionK.id[Option]
      }
    }
  }

  override def scalaCheckTestParameters =
    super
      .scalaCheckTestParameters
      .withMinSuccessfulTests(if (Platform.isJvm) 35 else 5)
      .withMaxDiscardRatio(if (Platform.isJvm) 5f else 50f)
      .withMinSize(0)
      .withWorkers(1)
}
