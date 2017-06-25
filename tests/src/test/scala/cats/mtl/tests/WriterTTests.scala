package cats
package mtl
package tests

import cats._
import cats.data._
import cats.instances.all._
import cats.mtl.instances.all._
import cats.laws.discipline.arbitrary._

class WriterTTests extends BaseSuite {
  checkAll("WriterT[Option, List[Int], ?]",
    ApplicativeTellTests[WriterTC[Option, List[Int]]#l, List[Int]].applicativeTell[String])
}
