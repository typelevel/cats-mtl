package cats
package mtl

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
  // test instances.all._
  {
    import cats.mtl.instances.all._
    test("asking") {
      assertCompiles("implicitly[applicative.Asking[ReaderStrId, String]]")

      assertCompiles("implicitly[applicative.Asking[ReaderStrInt, Int]]")

      assertCompiles("implicitly[applicative.Asking[ReaderStrInt, String]]")

      assertCompiles("implicitly[applicative.Asking[ReaderStrFuncInt, Int]]")
    }

    test("listening") {
      assertCompiles("implicitly[applicative.Listening[WriterStrId, String]]")

      assertCompiles("implicitly[applicative.Listening[WriterTStrWriterTInt, String]]")

      assertTypeError("implicitly[applicative.Listening[WriterTStrWriterTInt, Vector[Int]]]")

      assertTypeError("implicitly[applicative.Listening[WriterTStrTupleInt, Vector[Int]]]")
    }

    test("raising") {
      assertCompiles("implicitly[functor.Raising[EitherStrId, String]]")

      assertCompiles("implicitly[functor.Raising[EitherTStrEitherTInt, Int]]")

      assertCompiles("implicitly[functor.Raising[EitherTStrEitherTInt, String]]")
    }

    test("scoping") {
      assertCompiles("implicitly[applicative.Scoping[ReaderStrId, String]]")

      assertCompiles("implicitly[applicative.Scoping[ReaderStrInt, Int]]")

      assertCompiles("implicitly[applicative.Scoping[ReaderStrInt, String]]")

      assertCompiles("implicitly[applicative.Scoping[ReaderStrFuncInt, Int]]")
    }

    test("stateful") {
      assertCompiles("implicitly[monad.Stateful[StateStrId, String]]")

      assertCompiles("implicitly[monad.Stateful[StateTStrStateTInt, String]]")

      assertCompiles("implicitly[monad.Stateful[StateTStrStateTInt, Int]]")
    }

    test("telling") {
      assertCompiles("implicitly[applicative.Telling[WriterStrId, String]]")

      assertCompiles("implicitly[applicative.Telling[WriterTStrWriterTInt, String]]")

      assertCompiles("implicitly[applicative.Telling[WriterTStrWriterTInt, Vector[Int]]]")

      assertCompiles("implicitly[applicative.Telling[WriterTStrTupleInt, Vector[Int]]]")
    }

    test("tfunctor") {
      assertCompiles("implicitly[functor.TFunctor[StateTCS[String]#l]]")

      assertCompiles("implicitly[functor.TFunctor[OptionT]]")

      assertCompiles("implicitly[functor.TFunctor[ReaderTCE[String]#l]]")

      assertCompiles("implicitly[functor.TFunctor[WriterTCL[String]#l]]")

      // you *need* to flatten one of the layers in StateT
      // to access the applicative layers to `ap`
      assertTypeError("implicitly[applicative.TFunctor[StateTCS[String]#l]]")

      assertCompiles("implicitly[applicative.TFunctor[OptionT]]")

      assertCompiles("implicitly[applicative.TFunctor[ReaderTCE[String]#l]]")

      assertCompiles("implicitly[applicative.TFunctor[WriterTCL[String]#l]]")

      assertCompiles("implicitly[monad.TFunctor[StateTCS[String]#l]]")

      assertCompiles("implicitly[monad.TFunctor[OptionT]]")

      assertCompiles("implicitly[monad.TFunctor[ReaderTCE[String]#l]]")

      assertCompiles("implicitly[monad.TFunctor[WriterTCL[String]#l]]")
    }

    test("lift") {
      assertCompiles("implicitly[monad.Lift[EitherTC[Eval, String]#l, WriterTC[EitherTC[Eval, String]#l, String]#l]]")
      assertCompiles("implicitly[monad.Lift[WriterTC[EitherTC[Eval, String]#l, String]#l, OptionTC[WriterTC[EitherTC[Eval, String]#l, String]#l]#l]]")
    }
  }


}
