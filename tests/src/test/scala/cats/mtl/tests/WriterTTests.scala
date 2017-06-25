package cats
package mtl
package tests

import cats._
import cats.data._
import cats.instances.all._
import cats.laws.discipline.SerializableTests
import cats.mtl.instances.all._
import cats.laws.discipline.arbitrary._
import cats.mtl.laws.discipline.ApplicativeTellTests

class WriterTTests extends BaseSuite {
  checkAll("WriterT[Option, List[Int], List[Int]]",
    ApplicativeTellTests[WriterTC[Option, List[Int]]#l, List[Int]].applicativeTell[String])
  checkAll("ApplicativeTell[WriterT[Option, List[Int], ?]]",
    SerializableTests.serializable(ApplicativeTell[WriterTC[Option, List[Int]]#l, List[Int]]))
}
