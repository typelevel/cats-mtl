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
  checkAll("FunctorListen[(String, ?), String]",
    FunctorListenTests[TupleC[String]#l, String]
      .functorListen[String, String])
  checkAll("FunctorListen[(String, ?), String]",
    SerializableTests.serializable(FunctorListen[TupleC[String]#l, String]))
}

