package cats
package mtl

import data._
import cats.instances.string._
import cats.instances.vector._
import mtl.instances.writert._
import mtl.instances.telling._
import mtl.monad.Telling

class TellingUsage extends BaseSuite {

  test("tell") {
    val _ =
      Telling.tell[WriterTStrWriterTInt, Vector[Int]](Vector(1))
    val _1 =
      Telling.tell[WriterTStrWriterTInt, String]("err")
    val raiseFNoExpectedTypeWithArgsNestedInReader =
      Telling.tellF[WriterTStrWriterTInt]("err")
    assert(
      raiseFNoExpectedTypeWithArgsNestedInReader ==
        WriterT.put[WriterTIntId, String, Unit](())("err")
    )
  }

}
