package cats
package mtl
package tests


final class SummonableImplicits extends BaseSuite {

  // test instances.all._
  //noinspection ScalaUnusedSymbol
  {
    import cats.data._
    import cats.instances.all._
    import cats.mtl.instances.all._
    test("ApplicativeAsk") {
      assertCompiles("implicitly[ApplicativeAsk[ReaderStrId, String]]")

      assertCompiles("implicitly[ApplicativeAsk[ReaderStrInt, Int]]")

      assertCompiles("implicitly[ApplicativeAsk[ReaderStrInt, String]]")

      assertCompiles("implicitly[ApplicativeAsk[ReaderStrFuncInt, Int]]")
    }

    test("FunctorListen") {
      assertCompiles("implicitly[FunctorListen[WriterStrId, String]]")

      assertCompiles("implicitly[FunctorListen[WriterTStrWriterTInt, String]]")

      assertCompiles("implicitly[FunctorListen[WriterTStrWriterTInt, Vector[Int]]]")

      assertCompiles("implicitly[FunctorListen[WriterTStrTupleInt, Vector[Int]]]")
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

    test("FunctorTell") {
      assertCompiles("implicitly[FunctorTell[WriterStrId, String]]")

      assertCompiles("implicitly[FunctorTell[WriterTStrWriterTInt, String]]")

      assertCompiles("implicitly[FunctorTell[WriterTStrWriterTInt, Vector[Int]]]")

      assertCompiles("implicitly[FunctorTell[WriterTStrTupleInt, Vector[Int]]]")
    }

    test("TFunctor") {
      assertCompiles("implicitly[TFunctor[StateTCS[String]#l]]")

      assertCompiles("implicitly[TFunctor[OptionT]]")

      assertCompiles("implicitly[TFunctor[ReaderTCE[String]#l]]")

      assertCompiles("implicitly[TFunctor[WriterTCL[String]#l]]")
    }
  }


}
