package cats
package mtl
package tests

import cats._
import cats.data._
import cats.instances.all._
import cats.laws.discipline.SerializableTests
import cats.mtl.instances.all._
import cats.mtl.instances.local._
import cats.laws.discipline.arbitrary._
import cats.laws.discipline.eq._
import cats.mtl.laws.discipline.{ApplicativeLocalTests, FunctorEmptyTests, TraverseEmptyTests}

class EmptyTests extends BaseSuite {
  checkAll("Option",
    TraverseEmptyTests[Option](mtl.instances.traverseEmpty.optionTraverseEmpty)
      .traverseEmpty[String, String, String])
  checkAll("TraverseEmpty[Option]",
    SerializableTests.serializable(mtl.instances.traverseEmpty.optionTraverseEmpty))

  checkAll("List",
    TraverseEmptyTests[List](mtl.instances.traverseEmpty.listTraverseEmpty)
      .traverseEmpty[String, String, String])
  checkAll("TraverseEmpty[List]",
    SerializableTests.serializable(mtl.instances.traverseEmpty.listTraverseEmpty))

  checkAll("Vector",
    TraverseEmptyTests[Vector](mtl.instances.traverseEmpty.vectorTraverseEmpty)
      .traverseEmpty[String, String, String])
  checkAll("TraverseEmpty[Vector]",
    SerializableTests.serializable(mtl.instances.traverseEmpty.vectorTraverseEmpty))

  checkAll("Stream",
    TraverseEmptyTests[Stream](mtl.instances.traverseEmpty.streamTraverseEmpty)
      .traverseEmpty[String, String, String])
  checkAll("TraverseEmpty[Stream]",
    SerializableTests.serializable(mtl.instances.traverseEmpty.streamTraverseEmpty))
}
