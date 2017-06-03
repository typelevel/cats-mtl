package cats
package mtl

import instances.all._
import cats.instances.all._

final class SummonableImplicits extends BaseSuite {

  test("asking") {
    def _1 = implicitly[monad.Asking[ReaderStrId, String]]
    def _2 = implicitly[monad.Asking[ReaderStrInt, Int]]
    def _3 = implicitly[monad.Asking[ReaderStrInt, String]]
  }

  test("raising") {
    def _1 = implicitly[monad.Raising[EitherStrId, String]]
    def _2 = implicitly[monad.Raising[EitherTStrEitherTInt, Int]]
    def _3 = implicitly[monad.Raising[EitherTStrEitherTInt, String]]
  }

  test("scoping") {
    def _1 = implicitly[monad.Scoping[ReaderStrId, String]]
    def _2 = implicitly[monad.Scoping[ReaderStrInt, Int]]
    def _3 = implicitly[monad.Scoping[ReaderStrInt, String]]
  }

  test("stateful") {
    def _1 = implicitly[monad.Stateful[StateStrId, String]]
    def _2 = implicitly[monad.Stateful[StateTStrStateTInt, String]]
    def _3 = implicitly[monad.Stateful[StateTStrStateTInt, Int]]
  }

  test("telling") {
    def _1 = implicitly[monad.Telling[WriterStrId, String]]
    def _2 = implicitly[monad.Telling[WriterTStrWriterTInt, String]]
    def _3 = implicitly[monad.Telling[WriterTStrWriterTInt, Vector[Int]]]
  }


}
