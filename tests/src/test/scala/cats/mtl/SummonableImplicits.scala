package cats
package mtl

import instances.all._
import cats.instances.all._
import cats.data._

final class SummonableImplicits extends BaseSuite {

  private type ReaderStr[M[_], A] = ReaderT[M, String, A]
  private type ReaderStrId[A] = ReaderT[Id, String, A]
  private type ReaderInt[M[_], A] = ReaderT[M, Int, A]
  private type ReaderIntId[A] = Reader[Int, A]
  private type ReaderStrInt[A] = ReaderStr[ReaderIntId, A]
  private type ReaderStrFuncInt[A] = ReaderStr[FunctionC[Int]#l, A]

  private type EitherTStr[M[_], A] = EitherT[M, String, A]
  private type EitherStrId[A] = EitherT[Id, String, A]
  private type EitherTInt[M[_], A] = EitherT[M, Int, A]
  private type EitherTIntId[A] = EitherT[Id, Int, A]
  private type EitherTStrEitherTInt[A] = EitherTStr[EitherTIntId, A]

  private type StateTStr[M[_], A] = StateT[M, String, A]
  private type StateStrId[A] = StateT[Id, String, A]
  private type StateTInt[M[_], A] = StateT[M, Int, A]
  private type StateTIntId[A] = StateT[Id, Int, A]
  private type StateTStrStateTInt[A] = StateTStr[StateTIntId, A]

  private type WriterTStr[M[_], A] = WriterT[M, String, A]
  private type WriterStrId[A] = WriterT[Id, String, A]
  private type WriterTInt[M[_], A] = WriterT[M, Vector[Int], A]
  private type WriterTIntId[A] = WriterT[Id, Vector[Int], A]
  private type WriterTStrWriterTInt[A] = WriterTStr[WriterTIntId, A]
  private type WriterTStrTupleInt[A] = WriterTStr[TupleC[Vector[Int]]#l, A]

  //noinspection ScalaUnusedSymbol
  {
    test("asking") {
      def _1 = implicitly[monad.Asking[ReaderStrId, String]]

      def _2 = implicitly[monad.Asking[ReaderStrInt, Int]]

      def _3 = implicitly[monad.Asking[ReaderStrInt, String]]

      def _4 = implicitly[monad.Asking[ReaderStrFuncInt, Int]]
    }

    test("listening") {
      def _1 = implicitly[monad.Listening[WriterStrId, String]]

      def _2 = implicitly[monad.Listening[WriterTStrWriterTInt, String]]

      def _3 = implicitly[monad.Listening[WriterTStrWriterTInt, Vector[Int]]]

      def _4 = implicitly[monad.Listening[WriterTStrTupleInt, Vector[Int]]]
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

      def _4 = implicitly[monad.Scoping[ReaderStrFuncInt, Int]]
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

      def _4 = implicitly[monad.Telling[WriterTStrTupleInt, Vector[Int]]]
    }
  }


}
