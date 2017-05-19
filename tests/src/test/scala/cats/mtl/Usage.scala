package cats
package mtl

import cats.data._
import instances.ask._
import instances.readert._
import cats.implicits._

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
      Ask.ask[ReaderStrInt, Int]
    val _1 =
      Ask.ask[ReaderStrInt, String]
    val askFNoExpectedTypeWithArgsNestedInReader =
      Ask.askF[ReaderStrInt]()
    val _2: ReaderStrInt[Int] =
      askFNoExpectedTypeWithArgsNestedInReader
    val askFExpectedTypeWithArgsNestedInReader: ReaderStrInt[Int] =
      Ask.askF[ReaderStrInt]()
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
      Ask.reader[ReaderIntId, Int, String](_ + "!")
    val _1: ReaderIntId[String] =
      Ask.readerFE[ReaderIntId, Int](_ + "!")

  }

  test("summon") {
    implicitly[Ask[ReaderStrId, String]]
    implicitly[Ask[ReaderStrInt, Int]]
    implicitly[Ask[ReaderStrInt, String]]
  }

}
