package cats
package mtl
package tests

import cats._
import cats.data._
import cats.instances.all._
import cats.laws.discipline.SerializableTests
import cats.mtl.instances.all._
import cats.laws.discipline.arbitrary._
import cats.mtl.laws.discipline.ApplicativeListenTests

class TupleTests extends BaseSuite {
  checkAll("ApplicativeListen[(List[Int], ?), List[Int]]",
    ApplicativeListenTests[TupleC[List[Int]]#l, List[Int]].applicativeListen[String, String])
  checkAll("ApplicativeListen[(List[Int], ?)]",
    SerializableTests.serializable(ApplicativeListen[TupleC[List[Int]]#l, List[Int]]))
}

