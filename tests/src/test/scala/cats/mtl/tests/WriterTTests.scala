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
  checkAll("WriterT[Option, String, String]",
    FunctorListenTests[WriterTC[Option, String]#l, String].functorListen[String, String])
  checkAll("FunctorListen[WriterT[Option, String, ?]]",
    SerializableTests.serializable(FunctorListen[WriterTC[Option, String]#l, String]))
}
