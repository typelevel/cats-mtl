package cats
package mtl

import cats.data._
//import evidence._
//import cats.implicits._

class Usage extends BaseSuite {

  import shapeless.test._

  type ReaderStr[M[_], A] = ReaderT[M, String, A]
  type ReaderStrId[A] = ReaderT[Id, String, A]
  type ReaderInt[M[_], A] = ReaderT[M, Int, A]
  type ReaderIntId[A] = Reader[Int, A]
  type ReaderStrInt[A] = ReaderStr[ReaderIntId, A]

  // ask
  test("ask") {
    val askWithExpectedTypeNoArgsTopLevel: ReaderIntId[Int] =
      Ask.ask
    val askNoExpectedTypeWithArgsNestedInReader =
      Ask.ask[ReaderStrInt, Int]
    val askNoExpectedTypeWithArgsTopLevel =
      Ask.ask[ReaderStrInt, String]
    val askFNoExpectedTypeWithArgsNestedInReader =
      Ask.askF[ReaderStrInt]()
    val _: ReaderStrInt[Int] =
      askFNoExpectedTypeWithArgsNestedInReader
    val askFExpectedTypeWithArgsNestedInReader: ReaderStrInt[Int] =
      Ask.askF[ReaderStrInt]()
  }

  // reader
  test("reader") {
    val readerNoExpectedTypeWithAnnotatedLambdaArgNoArgs = Ask.reader((e: Int) => e + "!")
    val _: ReaderIntId[String] =
      readerNoExpectedTypeWithAnnotatedLambdaArgNoArgs
    illTyped {
      """
      val readerExpectedTypeNoAnnotatedArgNoArgs: ReaderIntId[String] =
        AskN.reader(e => e + "!"): ReaderIntId[String]
      """
    }
  }

  test("summon") {
    implicitly[Ask[ReaderStrId, String]]
    implicitly[Ask[ReaderStrInt, Int]]
    implicitly[Ask[ReaderStrInt, String]]
  }

}
