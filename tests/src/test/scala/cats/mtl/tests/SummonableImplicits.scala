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
    val ApplicativeAsk: Unit = {
      ApplicativeAsk[ReaderStrId, String]
      ApplicativeAsk[ReaderStrInt, Int]
      ApplicativeAsk[ReaderStrInt, String]
      ApplicativeAsk[ReaderStrFuncInt, Int]
    }

    val FunctorListen: Unit = {
      FunctorListen[WriterStrId, String]
      FunctorListen[WriterTStrWriterTInt, String]
      FunctorListen[WriterTStrWriterTInt, Vector[Int]]
      FunctorListen[WriterTStrTupleInt, Vector[Int]]
    }

    val FunctorRaise: Unit = {
      FunctorRaise[EitherStrId, String]
      FunctorRaise[EitherTStrEitherTInt, Int]
      FunctorRaise[EitherTStrEitherTInt, String]
    }

    val ApplicativeLocal: Unit = {
      ApplicativeLocal[ReaderStrId, String]
      ApplicativeLocal[ReaderStrInt, Int]
      ApplicativeLocal[ReaderStrInt, String]
      ApplicativeLocal[ReaderStrFuncInt, Int]
    }

    val MonadState: Unit = {
      MonadState[StateStrId, String]
      MonadState[StateTStrStateTInt, String]
      MonadState[StateTStrStateTInt, Int]
    }

    val FunctorTell: Unit = {
      FunctorTell[WriterStrId, String]
      FunctorTell[WriterTStrWriterTInt, String]
      FunctorTell[WriterTStrWriterTInt, Vector[Int]]
      FunctorTell[WriterTStrTupleInt, Vector[Int]]
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

    def traverseEmptyToFunctorEmpty[F[_] : TraverseEmpty]: FunctorEmpty[F] = {
      FunctorEmpty[F]
    }
  }


}
