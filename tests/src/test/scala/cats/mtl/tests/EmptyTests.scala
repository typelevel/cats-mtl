package cats
package mtl
package tests

import cats._
import cats.data._
import cats.instances.all._
import cats.laws.discipline.SerializableTests
import cats.mtl.instances.all._
import cats.laws.discipline.arbitrary._
import cats.laws.discipline.eq._
import cats.mtl.laws.discipline.{ApplicativeLocalTests, FunctorEmptyTests, TraverseEmptyTests}

class EmptyTests extends BaseSuite {
  checkAll("Option",
    TraverseEmptyTests[Option](mtl.instances.empty.optionTraverseEmpty)
      .traverseEmpty[String, String, String])
  checkAll("TraverseEmpty[Option]",
    SerializableTests.serializable(mtl.instances.empty.optionTraverseEmpty))

  checkAll("List",
    TraverseEmptyTests[List](mtl.instances.empty.listTraverseEmpty)
      .traverseEmpty[String, String, String])
  checkAll("TraverseEmpty[List]",
    SerializableTests.serializable(mtl.instances.empty.listTraverseEmpty))

  checkAll("Map[Int, ?]",
    FunctorEmptyTests[MapC[Int]#l](mtl.instances.empty.mapFunctorEmpty[Int])
      .functorEmpty[String, String, String])
  checkAll("FunctorEmpty[Map[Int, ?]]",
    SerializableTests.serializable(mtl.instances.empty.mapFunctorEmpty[Int]))

  checkAll("SortedMap[Int, ?]",
    TraverseEmptyTests[SortedMapC[Int]#l](mtl.instances.empty.sortedMapTraverseEmpty[Int])
      .traverseEmpty[String, String, String])
  checkAll("TraverseEmpty[SortedMap[Int, ?]]",
    SerializableTests.serializable(mtl.instances.empty.sortedMapTraverseEmpty[Int]))

  checkAll("Vector",
    TraverseEmptyTests[Vector](mtl.instances.empty.vectorTraverseEmpty)
      .traverseEmpty[String, String, String])
  checkAll("TraverseEmpty[Vector]",
    SerializableTests.serializable(mtl.instances.empty.vectorTraverseEmpty))

  checkAll("Const[Int, ?]",
    TraverseEmptyTests[Const[Int, ?]](mtl.instances.empty.constTraverseEmpty[Int])
      .traverseEmpty[String, String, String])
  checkAll("TraverseEmpty[Const[Int, ?]",
    SerializableTests.serializable(mtl.instances.empty.constTraverseEmpty[Int]))

  checkAll("Stream",
    TraverseEmptyTests[Stream](mtl.instances.empty.streamTraverseEmpty)
      .traverseEmpty[String, String, String])
  checkAll("TraverseEmpty[Stream]",
    SerializableTests.serializable(mtl.instances.empty.streamTraverseEmpty))

  checkAll("EitherT[List, Int, ?]",
    TraverseEmptyTests[EitherTC[List, Int]#l](mtl.instances.empty.traverseEmptyLiftEitherT(mtl.instances.empty.listTraverseEmpty))
      .traverseEmpty[String, String, String])
  checkAll("FunctorEmpty[OptionT[List, ?]]",
    SerializableTests.serializable(mtl.instances.empty.traverseEmptyLiftEitherT(mtl.instances.empty.listTraverseEmpty)))

  checkAll("Nested[List, List, ?]",
    TraverseEmptyTests[NestedC[List, List]#l](
      mtl.instances.empty.catsDataTraverseEmptyForNested[List, List](
        mtl.instances.empty.listTraverseEmpty.traverse,
        mtl.instances.empty.listTraverseEmpty)
    )
      .traverseEmpty[String, String, String])
  checkAll("TraverseEmpty[Nested[List, List, ?]]",
    SerializableTests.serializable(mtl.instances.empty.catsDataTraverseEmptyForNested[List, List](
      mtl.instances.empty.listTraverseEmpty.traverse,
      mtl.instances.empty.listTraverseEmpty)
    )
  )

  checkAll("OptionT[List, ?]",
    FunctorEmptyTests[OptionTC[List]#l](mtl.instances.empty.optionTFunctorEmpty[List])
      .functorEmpty[String, String, String])
  checkAll("TraverseEmpty[OptionT[List, ?]]",
    SerializableTests.serializable(mtl.instances.empty.optionTFunctorEmpty[List]))
}
