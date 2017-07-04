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

class TupleTests extends BaseSuite {
  checkAll("FunctorListen[(List[Int], ?), List[Int]]",
    FunctorListenTests[TupleC[List[Int]]#l, List[Int]].applicativeListen[String, String])
  checkAll("FunctorListen[(List[Int], ?), List[Int]]",
    SerializableTests.serializable(FunctorListen[TupleC[List[Int]]#l, List[Int]]))
}

