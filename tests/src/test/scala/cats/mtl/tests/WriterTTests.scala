package cats
package mtl
package tests

import cats._
import cats.arrow.FunctionK
import cats.data._
import cats.instances.all._
import cats.laws.discipline.{ExhaustiveCheck, SerializableTests}
import cats.laws.discipline.eq._
import cats.laws.discipline.arbitrary._
import cats.mtl.laws.discipline._
import org.scalacheck._

class WriterTTests extends BaseSuite {
  implicit val arbFunctionK: Arbitrary[Option ~> Option] =
    Arbitrary(Gen.oneOf(new (Option ~> Option) {
      def apply[A](fa: Option[A]): Option[A] = None
    }, FunctionK.id[Option]))

  implicit def eqKleisli[F[_], A, B](implicit arb: ExhaustiveCheck[A], ev: Eq[F[B]]): Eq[Kleisli[F, A, B]] = {
    Eq.by((x: (Kleisli[F, A, B])) => x.run)
  }

  implicit def stateTEq[F[_], S, A](implicit S: ExhaustiveCheck[S], FSA: Eq[F[(S, A)]], F: FlatMap[F]): Eq[StateT[F, S, A]] = {
    Eq.by[StateT[F, S, A], S => F[(S, A)]](state =>
      s => state.run(s))
  }

  type WriterTStringOverWriterTStringOverOption[A] = WriterT[WriterTC[Option, String]#l, List[Int], A]

  FunctorTell[Writer[Chain[String], *], Chain[String]]

  {
    import cats.laws.discipline.arbitrary._


    checkAll("WriterT[WriterTC[Option, String]#l, List[Int], String]",
      ApplicativeCensorTests[WriterTStringOverWriterTStringOverOption, String]
      .applicativeCensor[String, String]
    )
    checkAll("ApplicativePass[WriterT[WriterTC[Option, String]#l, List[Int], String]",
      SerializableTests.serializable(ApplicativeCensor[WriterTStringOverWriterTStringOverOption, String]))


    checkAll("ReaderT[WriterTC[Option, String]#l, List[Int], String]",
      ApplicativeCensorTests[ReaderTStringOverWriterTStringOverOption, String]
      .applicativeCensor[String, String])
    checkAll("ApplicativePass[ReaderT[WriterTC[Option, String]#l, List[Int], String]",
      SerializableTests.serializable(ApplicativeCensor[ReaderTStringOverWriterTStringOverOption, String]))

    checkAll("StateT[WriterTC[Option, String]#l, List[Int], String]",
      ApplicativeCensorTests[StateTStringOverWriterTStringOverOption, String]
      .applicativeCensor[String, String])
    checkAll("ApplicativePass[StateT[WriterTC[Option, String]#l, List[Int], String]",
      SerializableTests.serializable(ApplicativeCensor[StateTStringOverWriterTStringOverOption, String]))
  }

  {
    import cats.laws.discipline.arbitrary._

    checkAll("WriterT[Option, String, String]",
      ApplicativeCensorTests[WriterTC[Option, String]#l, String]
        .applicativeCensor[String, String])
    checkAll("FunctorListen[WriterT[Option, String, *]]",
      SerializableTests.serializable(FunctorListen[WriterTC[Option, String]#l, String]))
  }
}
