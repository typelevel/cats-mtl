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
    val ApplicativeAskTest: Unit = {
      def _1 = ApplicativeAsk[ReaderStrId, String]
      def _2 = ApplicativeAsk[ReaderStrInt, Int]
      def _3 = ApplicativeAsk[ReaderStrInt, String]
      def _4 = ApplicativeAsk[ReaderStrFuncInt, Int]
    }

    val FunctorListenTest: Unit = {
      def _1 = FunctorListen[WriterStrId, String]
      def _2 = FunctorListen[WriterTStrWriterTInt, String]
      def _3 = FunctorListen[WriterTStrWriterTInt, Vector[Int]]
      def _4 = FunctorListen[WriterTStrTupleInt, Vector[Int]]
    }

    val FunctorRaiseTest: Unit = {
      def _1 = FunctorRaise[EitherStrId, String]
      def _2 = FunctorRaise[EitherTStrEitherTInt, Int]
      def _3 = FunctorRaise[EitherTStrEitherTInt, String]
    }

    val ApplicativeLocalTest: Unit = {
      def _1 = ApplicativeLocal[ReaderStrId, String]
      def _2 = ApplicativeLocal[ReaderStrInt, Int]
      def _3 = ApplicativeLocal[ReaderStrInt, String]
      def _4 = ApplicativeLocal[ReaderStrFuncInt, Int]
    }

    val MonadStateTest: Unit = {
      def _1 = MonadState[StateStrId, String]
      def _2 = MonadState[StateTStrStateTInt, String]
      def _3 = MonadState[StateTStrStateTInt, Int]
    }

    val FunctorTellTest: Unit = {
      def _1 = FunctorTell[WriterStrId, String]
      def _2 = FunctorTell[WriterTStrWriterTInt, String]
      def _3 = FunctorTell[WriterTStrWriterTInt, Vector[Int]]
      def _4 = FunctorTell[WriterTStrTupleInt, Vector[Int]]
    }
  }

  // test hierarchy.base
  //noinspection ScalaUnusedSymbol
  locally {
    import cats.data._
    import cats.instances.all._
    import cats.mtl.implicits._

    def localToAsk[F[_]](implicit F: ApplicativeLocal[F, String]): ApplicativeAsk[F, String] = {
      ApplicativeAsk[F, String]
    }

    def listenToTell[F[_]](implicit F: FunctorListen[F, String]): FunctorTell[F, String] = {
      FunctorTell[F, String]
    }

    def stateToTell[F[_]](implicit F: FunctorTell[F, String]): FunctorTell[F, String] = {
      FunctorTell[F, String]
    }

    def stateToAsk[F[_]](implicit F: ApplicativeAsk[F, String]): ApplicativeAsk[F, String] = {
      ApplicativeAsk[F, String]
    }

  }


}
