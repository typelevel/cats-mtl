package cats
package mtl

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

  // test instances.all._
  //noinspection ScalaUnusedSymbol
  {
    import cats.instances.all._
    import cats.mtl.instances.all._
    test("ApplicativeAsk") {
      assertCompiles("implicitly[ApplicativeAsk[ReaderStrId, String]]")

      assertCompiles("implicitly[ApplicativeAsk[ReaderStrInt, Int]]")

      assertCompiles("implicitly[ApplicativeAsk[ReaderStrInt, String]]")

      assertCompiles("implicitly[ApplicativeAsk[ReaderStrFuncInt, Int]]")
    }

    test("ApplicativeListen") {
      assertCompiles("implicitly[ApplicativeListen[WriterStrId, String]]")

      assertCompiles("implicitly[ApplicativeListen[WriterTStrWriterTInt, String]]")

      assertTypeError("implicitly[ApplicativeListen[WriterTStrWriterTInt, Vector[Int]]]")

      assertTypeError("implicitly[ApplicativeListen[WriterTStrTupleInt, Vector[Int]]]")
    }

    test("FunctorRaise") {
      assertCompiles("implicitly[FunctorRaise[EitherStrId, String]]")

      assertCompiles("implicitly[FunctorRaise[EitherTStrEitherTInt, Int]]")

      assertCompiles("implicitly[FunctorRaise[EitherTStrEitherTInt, String]]")
    }

    test("ApplicativeLocal") {
      assertCompiles("implicitly[ApplicativeLocal[ReaderStrId, String]]")

      assertCompiles("implicitly[ApplicativeLocal[ReaderStrInt, Int]]")

      assertCompiles("implicitly[ApplicativeLocal[ReaderStrInt, String]]")

      assertCompiles("implicitly[ApplicativeLocal[ReaderStrFuncInt, Int]]")
    }

    test("MonadState") {
      assertCompiles("implicitly[MonadState[StateStrId, String]]")

      assertCompiles("implicitly[MonadState[StateTStrStateTInt, String]]")

      assertCompiles("implicitly[MonadState[StateTStrStateTInt, Int]]")
    }

    test("ApplicativeTell") {
      assertCompiles("implicitly[ApplicativeTell[WriterStrId, String]]")

      assertCompiles("implicitly[ApplicativeTell[WriterTStrWriterTInt, String]]")

      assertCompiles("implicitly[ApplicativeTell[WriterTStrWriterTInt, Vector[Int]]]")

      assertCompiles("implicitly[ApplicativeTell[WriterTStrTupleInt, Vector[Int]]]")
    }

    test("XFunctor") {
      assertCompiles("implicitly[FunctorFunctor[StateTCS[String]#l]]")

      assertCompiles("implicitly[FunctorFunctor[OptionT]]")

      assertCompiles("implicitly[FunctorFunctor[ReaderTCE[String]#l]]")

      assertCompiles("implicitly[FunctorFunctor[WriterTCL[String]#l]]")

      // you *need* to flatten one of the layers in StateT
      // to access the applicative layers to `ap`
      assertTypeError("implicitly[ApplicativeFunctor[StateTCS[String]#l]]")

      assertCompiles("implicitly[ApplicativeFunctor[OptionT]]")

      assertCompiles("implicitly[ApplicativeFunctor[ReaderTCE[String]#l]]")

      assertCompiles("implicitly[ApplicativeFunctor[WriterTCL[String]#l]]")

      assertCompiles("implicitly[MonadFunctor[StateTCS[String]#l]]")

      assertCompiles("implicitly[MonadFunctor[OptionT]]")

      assertCompiles("implicitly[MonadFunctor[ReaderTCE[String]#l]]")

      assertCompiles("implicitly[MonadFunctor[WriterTCL[String]#l]]")
    }

    test("lift") {
      assertCompiles(
        "implicitly[MonadLift[EitherTC[Eval, String]#l, WriterTC[EitherTC[Eval, String]#l, String]#l]]"
      )
      assertCompiles(
        "implicitly[MonadLift[WriterTC[EitherTC[Eval, String]#l, String]#l, OptionTC[WriterTC[EitherTC[Eval, String]#l, String]#l]#l]]"
      )
    }
  }


}
