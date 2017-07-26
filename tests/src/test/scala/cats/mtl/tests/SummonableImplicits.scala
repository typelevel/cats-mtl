package cats
package mtl
package tests


final class SummonableImplicits extends BaseSuite {

  // test instances.all._
  //noinspection ScalaUnusedSymbol
  locally {
    import cats.data._
    import cats.instances.all._
    import cats.mtl.implicits._
    def ApplicativeAsk: Unit = {
      val _1 = implicitly[ApplicativeAsk[ReaderStrId, String]]

      val _2 = implicitly[ApplicativeAsk[ReaderStrInt, Int]]

      val _3 = implicitly[ApplicativeAsk[ReaderStrInt, String]]

      val _4 = implicitly[ApplicativeAsk[ReaderStrFuncInt, Int]]
    }

    def FunctorListen: Unit = {
      val _1 = implicitly[FunctorListen[WriterStrId, String]]

      val _2 = implicitly[FunctorListen[WriterTStrWriterTInt, String]]

      val _3 = implicitly[FunctorListen[WriterTStrWriterTInt, Vector[Int]]]

      val _4 = implicitly[FunctorListen[WriterTStrTupleInt, Vector[Int]]]
    }

    def FunctorRaise: Unit = {
      val _1 = implicitly[FunctorRaise[EitherStrId, String]]

      val _2 = implicitly[FunctorRaise[EitherTStrEitherTInt, Int]]

      val _3 = implicitly[FunctorRaise[EitherTStrEitherTInt, String]]
    }

    def ApplicativeLocal: Unit = {
      val _1 = implicitly[ApplicativeLocal[ReaderStrId, String]]

      val _2 = implicitly[ApplicativeLocal[ReaderStrInt, Int]]

      val _3 = implicitly[ApplicativeLocal[ReaderStrInt, String]]

      val _4 = implicitly[ApplicativeLocal[ReaderStrFuncInt, Int]]
    }

    def MonadState: Unit = {
      val _1 = implicitly[MonadState[StateStrId, String]]

      val _2 = implicitly[MonadState[StateTStrStateTInt, String]]

      val _3 = implicitly[MonadState[StateTStrStateTInt, Int]]
    }

    def FunctorTell: Unit = {
      val _1 = implicitly[FunctorTell[WriterStrId, String]]

      val _2 = implicitly[FunctorTell[WriterTStrWriterTInt, String]]

      val _3 = implicitly[FunctorTell[WriterTStrWriterTInt, Vector[Int]]]

      val _4 = implicitly[FunctorTell[WriterTStrTupleInt, Vector[Int]]]
    }
  }


}
