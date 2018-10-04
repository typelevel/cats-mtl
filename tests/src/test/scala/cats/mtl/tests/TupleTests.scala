package cats
package mtl
package tests

import cats._
import cats.data._
import cats.instances.all._
import cats.laws.discipline.SerializableTests
import cats.mtl.instances.all._
import cats.laws.discipline.arbitrary._
import cats.mtl.laws.discipline.ApplicativeCensorTests

class TupleTests extends BaseSuite {
  checkAll("ApplicativeCensor[(String, ?), String]",
    ApplicativeCensorTests[TupleC[String]#l, String]
      .applicativeCensor[String, String])
  checkAll("ApplicativeCensor[(String, ?), String]",
    SerializableTests.serializable(ApplicativeCensor[TupleC[String]#l, String]))
}

