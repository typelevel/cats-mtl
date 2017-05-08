package cats
package mtl

import cats.data._
import cats.mtl.evidence._

object Usage {

  type ReaderStr[A] = Reader[String, A]

  import evidence.Capabilities._

//  implicitly[Ask[String, ReaderStr]]
  implicitly[Find[EffAsk[String], ReaderStr]]
//  implicitly[Find[EffAsk[Int], ReaderStr]]
//  val a: Ask[String, ReaderStr] =
//    AskN.askNReader[Id, String]

}
