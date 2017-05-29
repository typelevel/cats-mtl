package cats
package mtl

import cats.data._
import instances.asking._
import instances.readert._
import cats.implicits._
import cats.mtl.monad.Asking

class Usage extends BaseSuite {

  import shapeless.test._

  type ReaderStr[M[_], A] = ReaderT[M, String, A]
  type ReaderStrId[A] = ReaderT[Id, String, A]
  type ReaderInt[M[_], A] = ReaderT[M, Int, A]
  type ReaderIntId[A] = Reader[Int, A]
  type ReaderStrInt[A] = ReaderStr[ReaderIntId, A]

  // ask
  test("ask") {
    val _ =
      Asking.ask[ReaderStrInt, Int]
    val _1 =
      Asking.ask[ReaderStrInt, String]
    val askFNoExpectedTypeWithArgsNestedInReader =
      Asking.askF[ReaderStrInt]()
    val _2: ReaderStrInt[Int] =
      askFNoExpectedTypeWithArgsNestedInReader
    val askFExpectedTypeWithArgsNestedInReader: ReaderStrInt[Int] =
      Asking.askF[ReaderStrInt]()
  }

  // reader
  test("reader") {
    illTyped {
      """
      val readerExpectedTypeWithAnnotatedLambdaArgNoArgs: ReaderIntId[String] = Ask.reader((e: Int) => e + "!")
      """
    }
    illTyped {
      """
      val readerExpectedTypeNoAnnotatedArgNoArgs: ReaderIntId[String] =
        AskN.reader(e => e + "!"): ReaderIntId[String]
      """
    }
    val _ =
      Asking.reader[ReaderIntId, Int, String](_ + "!")
    val _1: ReaderIntId[String] =
      Asking.readerFE[ReaderIntId, Int](_ + "!")

  }

  test("summon") {
    implicitly[Asking[ReaderStrId, String]]
    implicitly[Asking[ReaderStrInt, Int]]
    implicitly[Asking[ReaderStrInt, String]]
  }

}
