package cats
package mtl

import cats.data._
//import evidence._
//import cats.implicits._

object Usage {

  type ReaderStr[M[_], A] = ReaderT[M, String, A]
  type ReaderStrId[A] = ReaderT[Id, String, A]
  type ReaderInt[M[_], A] = ReaderT[M, Int, A]
  type ReaderIntId[A] = Reader[Int, A]
  type ReaderStrInt[A] = ReaderStr[ReaderIntId, A]

  // ask
  locally {
    val askWithExpectedTypeNoArgs: ReaderIntId[Int] =
      AskN.ask
    val askNoExpectedTypeWithArgs =
      AskN.ask[ReaderStrInt, Int]
    val askNoExpectedTypeWithArgsNestedInReader =
      AskN.ask[ReaderStrInt, String]
    val askFNoExpectedTypeWithArgs =
      AskN.askF[ReaderStrInt]()
    val _: ReaderStrInt[Int] =
      askFNoExpectedTypeWithArgs
    val askFExpectedTypeWithArgsNestedInReader: ReaderStrInt[Int] =
      AskN.askF[ReaderStrInt]()
  }

  // reader
  locally {
    val readerNoExpectedTypeWithAnnotatedLambdaArgNoArgs = AskN.reader((e: Int) => e + "!")
    val _: ReaderIntId[String] =
      readerNoExpectedTypeWithAnnotatedLambdaArgNoArgs
    import shapeless.test._
    illTyped {
      """
      val readerExpectedTypeNoAnnotatedArgNoArgs: ReaderIntId[String] =
        AskN.reader(e => e + "!"): ReaderIntId[String]
      """
    }
  }

  implicitly[AskN[ReaderStrId, String]]
  implicitly[AskN[ReaderStrInt, Int]]
  implicitly[AskN[ReaderStrInt, String]]
  //  implicitly[Ask[Int, ReaderStrInt]] // resolves
  //  implicitly[Find[EffAsk[String], ReaderStrInt]] // resolves
  //  implicitly[Find[EffAsk[Int], ReaderIntId]] // resolves
  //  implicitly[Find[EffAsk[Int], ReaderStrInt]] // resolves

  // explicit implicits
  //  Find.findTCons[EffAsk[String], ReaderStr[Id, ?]](
  //    readerCanAsk[String, Id]
  //  ): Find[EffAsk[String], ReaderStr[Id, ?]]
  //  Find.findTCons[EffAsk[String], ReaderStrInt](
  //    readerCanAsk[String, ReaderInt[Id, ?]]
  //  ): Find[EffAsk[String], ReaderStrInt]
  //  Find.findFCons[EffAsk[Int], ReaderStr, ReaderInt[Id, ?]](
  //    Find.findTCons[EffAsk[Int], ReaderInt[Id, ?]](readerCanAsk[Int, Id])
  //  ): Find[EffAsk[Int], ReaderStrInt]

}
