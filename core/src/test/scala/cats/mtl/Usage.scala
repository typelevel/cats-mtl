package cats
package mtl

import cats.data._
//import evidence._
//import cats.implicits._

object Usage {

  type ReaderStr[M[_], A] = ReaderT[String, M, A]
  type ReaderStrId[A] = ReaderT[String, Id, A]
  type ReaderInt[M[_], A] = ReaderT[Int, M, A]
  type ReaderIntId[A] = Reader[Int, A]
  type ReaderStrInt[A] = ReaderStr[ReaderIntId, A]

  val x: ReaderIntId[Int] = AskN.ask
  val y: ReaderStrInt[Int] =
    AskN.askF[ReaderStrInt]() // (AskN.askNInd[Nat.Zero, ReaderStr, ReaderIntId, Int])
  val z: ReaderStrInt[String] =
    AskN.askF[ReaderStrInt]() // (AskN.askNInd[Nat.Zero, ReaderStr, ReaderIntId, Int])

  implicitly[AskN[ReaderStrId, String]]
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
