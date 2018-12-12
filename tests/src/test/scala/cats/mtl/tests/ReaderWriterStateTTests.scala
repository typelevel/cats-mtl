package cats.mtl.tests

import cats.arrow.FunctionK
import cats.{Eq, Monad, Monoid, ~>}
import cats.data.{IndexedReaderWriterStateT, ReaderWriterState, ReaderWriterStateT}
import cats.implicits._
import cats.laws.discipline.arbitrary._
import cats.laws.discipline._
import cats.laws.discipline.eq._
import cats.mtl._
import cats.mtl.implicits._
import cats.mtl.laws.discipline._
import cats.mtl.lifting.MonadLayerControl
import org.scalacheck.{Arbitrary, Gen}


class ReaderWriterStateTTests extends StateTTestsBase {

  implicit def RWSTEq[F[_], E, L, S, A](implicit S: Arbitrary[S], E: Arbitrary[E],
                                              FLSB: Eq[F[(L, S, A)]], F: Monad[F]): Eq[ReaderWriterStateT[F, E, L, S, A]] =
    Eq.by[ReaderWriterStateT[F, E, L, S, A], (E, S) => F[(L, S, A)]] { state =>
      (e, s) => state.run(e, s)
    }

  implicit def arbFunctionKTupled[L: Monoid, S]: Arbitrary[(L, S, ?) ~> (L, S, ?)] =
    Arbitrary(Gen.oneOf(new ((L, S, ?) ~> (L, S, ?)) {
      def apply[A](fa: (L, S, A)): (L, S, A) = (Monoid[L].empty, fa._2, fa._3)
    }, FunctionK.id[(L, S, ?)]))

  checkAll("ReaderWriterStateT[Option, Boolean, Int, String, ?]",
    MonadLayerControlTests[ReaderWriterStateT[Option, Boolean, Int, String, ?], Option, (Int, String, ?)]
      .monadLayerControl[Boolean, Boolean])
  checkAll("MonadLayerControl[ReaderWriterStateT[Option, Boolean, Int, String, ?], Option]",
    SerializableTests.serializable(MonadLayerControl[ReaderWriterStateT[Option, Boolean, Int, String, ?], Option]))

  checkAll("ReaderWriterState[Boolean, Int, String, String]",
    MonadStateTests[ReaderWriterState[Boolean, Int, String, ?], String]
      .monadState[String])
  checkAll("MonadState[ReaderWriterState[Boolean, Int, String, ?]]",
    SerializableTests.serializable(MonadState[ReaderWriterState[Boolean, Int, String, ?], String]))

  checkAll("ReaderWriterStateT[Option, Boolean, Int, String, String]",
    MonadStateTests[ReaderWriterStateT[Option, Boolean, Int, String, ?], String]
      .monadState[String])
  checkAll("MonadState[ReaderWriterStateT[Option, Boolean, Int, String, ?]]",
    SerializableTests.serializable(MonadState[ReaderWriterStateT[Option, Boolean, Int, String, ?], String]))

  checkAll("ReaderWriterStateT[Option, Boolean, Int, String, Int]",
    ApplicativeCensorTests[ReaderWriterStateT[Option, Boolean, Int, String, ?], Int]
      .applicativeCensor[Int, String])
  checkAll("ApplicativeCensor[ReaderWriterStateT[Option, Boolean, Int, String, ?]]",
    SerializableTests.serializable(ApplicativeCensor[ReaderWriterStateT[Option, Boolean, Int, String, ?], Int]))

  checkAll("ReaderWriterStateT[Option, Boolean, Int, String, Boolean]",
    ApplicativeLocalTests[ReaderWriterStateT[Option, Boolean, Int, String, ?], Boolean]
      .applicativeLocal[Boolean, String])
  checkAll("ApplicativeLocal[ReaderWriterStateT[Option, Boolean, Int, String, ?]]",
    SerializableTests.serializable(ApplicativeLocal[ReaderWriterStateT[Option, Boolean, Int, String, ?], Boolean]))

}
