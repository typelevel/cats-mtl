package cats
package mtl


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
