package cats
package mtl
package tests

import cats._
import cats.data._
import cats.instances.all._
import cats.laws.discipline.SerializableTests
import cats.mtl.instances.all._
import cats.laws.discipline.arbitrary._
import cats.mtl.laws.discipline.FunctorListenTests

class WriterTTests extends BaseSuite {
  checkAll("WriterT[Option, List[Int], List[Int]]",
    FunctorListenTests[WriterTC[Option, List[Int]]#l, List[Int]].applicativeListen[String, String])
  checkAll("FunctorListen[WriterT[Option, List[Int], ?]]",
    SerializableTests.serializable(FunctorListen[WriterTC[Option, List[Int]]#l, List[Int]]))
}
