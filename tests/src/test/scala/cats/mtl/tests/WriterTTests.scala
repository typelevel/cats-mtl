package cats
package mtl
package tests

import cats._
import cats.arrow.FunctionK
import cats.data._
import cats.instances.all._
import cats.laws.discipline.SerializableTests
import cats.mtl.instances.listen._
import cats.laws.discipline.eq._
import cats.mtl.laws.discipline._
import org.scalacheck._

class WriterTTests extends BaseSuite {
  implicit val arbFunctionK: Arbitrary[Option ~> Option] =
    Arbitrary(Gen.oneOf(new (Option ~> Option) {
      def apply[A](fa: Option[A]): Option[A] = None
    }, FunctionK.id[Option]))

  implicit def eqKleisli[F[_], A, B](implicit arb: Arbitrary[A], ev: Eq[F[B]]): Eq[Kleisli[F, A, B]] =
    Eq.by((x: (Kleisli[F, A, B])) => x.run)

  implicit def catsLawArbitraryForStateT[F[_], S, A](implicit F: Arbitrary[F[S => F[(S, A)]]]): Arbitrary[StateT[F, S, A]] = {
    Arbitrary(F.arbitrary.map(StateT.applyF))
  }

  implicit def stateTEq[F[_], S, A](implicit S: Arbitrary[S], FSA: Eq[F[(S, A)]], F: FlatMap[F]): Eq[StateT[F, S, A]] = {
    Eq.by[StateT[F, S, A], S => F[(S, A)]](state =>
      s => state.run(s))
  }

  type WriterTStringOverWriterTStringOverOption[A] = WriterT[WriterTC[Option, String]#l, List[Int], A]

  locally {
    // I know, this is unbelievably odd.
    // The reason this is in its own scope is that the Arbitrary and Eq instance induction for WriterT
    // is detected as "divergent" by 2.10, so I have to isolate the *rest* of the imports in the file,
    locally {
      import cats.mtl.instances.listen._, cats.mtl.instances.writert._
//      implicit val listen = FunctorListen[WriterTStringOverWriterTStringOverOption, String]
      implicit val arb = cats.laws.discipline.arbitrary.catsLawsArbitraryForWriterT[WriterTC[Option, String]#l, List[Int], String](
        cats.laws.discipline.arbitrary.catsLawsArbitraryForWriterT[Option, String, (List[Int], String)]
      )
      implicit val eqS = WriterT.catsDataEqForWriterT[WriterTC[Option, String]#l, List[Int], String](
        WriterT.catsDataEqForWriterT[Option, String, (List[Int], String)]
      )
      implicit val eqSS = WriterT.catsDataEqForWriterT[WriterTC[Option, String]#l, List[Int], (String, String)](
        WriterT.catsDataEqForWriterT[Option, String, (List[Int], (String, String))]
      )
      implicit val eqUS = WriterT.catsDataEqForWriterT[WriterTC[Option, String]#l, List[Int], (Unit, String)](
        WriterT.catsDataEqForWriterT[Option, String, (List[Int], (Unit, String))]
      )
      implicit val eqU = WriterT.catsDataEqForWriterT[WriterTC[Option, String]#l, List[Int], Unit](
        WriterT.catsDataEqForWriterT[Option, String, (List[Int], Unit)]
      )
      // this diverges in 2.10. Maybe just don't promise it?
      checkAll("WriterT[WriterTC[Option, String]#l, List[Int], String]",
        FunctorListenTests[WriterTStringOverWriterTStringOverOption, String].functorListen[String, String](
          implicitly, implicitly,
          implicitly, implicitly, implicitly, implicitly,
          implicitly, implicitly, implicitly, implicitly
        )
      )
      checkAll("FunctorListen[WriterT[WriterTC[Option, String]#l, List[Int], String]",
        SerializableTests.serializable(FunctorListen[WriterTStringOverWriterTStringOverOption, String]))
    }

    locally {
      import cats.laws.discipline.arbitrary._
      import cats.mtl.instances.all._

      checkAll("ReaderT[WriterTC[Option, String]#l, List[Int], String]",
        FunctorListenTests[ReaderTStringOverWriterTStringOverOption, String].functorListen[String, String])
      checkAll("FunctorListen[ReaderT[WriterTC[Option, String]#l, List[Int], String]",
        SerializableTests.serializable(FunctorListen[ReaderTStringOverWriterTStringOverOption, String]))

      checkAll("StateT[WriterTC[Option, String]#l, List[Int], String]",
        FunctorListenTests[StateTStringOverWriterTStringOverOption, String].functorListen[String, String])
      checkAll("FunctorListen[StateT[WriterTC[Option, String]#l, List[Int], String]",
        SerializableTests.serializable(FunctorListen[StateTStringOverWriterTStringOverOption, String]))
    }
  }

  locally {
    import cats.laws.discipline.arbitrary._

    checkAll("WriterT[Option, String, String]",
      FunctorListenTests[WriterTC[Option, String]#l, String].functorListen[String, String])
    checkAll("FunctorListen[WriterT[Option, String, ?]]",
      SerializableTests.serializable(FunctorListen[WriterTC[Option, String]#l, String]))

    {
      implicit val monadLayerControl: MonadLayerControl[WriterTC[Option, String]#l, Option] =
        cats.mtl.instances.writert.writerMonadLayerControl[Option, String]
      checkAll("WriterT[Option, String, ?]",
        MonadLayerControlTests[WriterTC[Option, String]#l, Option].monadLayerControl[Boolean, Boolean])
      checkAll("MonadLayerControl[WriterT[Option, String, ?], Option]",
        SerializableTests.serializable(monadLayerControl))
    }

    {
      implicit val applicativeLayerFunctor: ApplicativeLayerFunctor[WriterTC[Option, String]#l, Option] =
        cats.mtl.instances.writert.writerApplicativeLayerFunctor[Option, String]
      checkAll("WriterT[Option, String, ?]",
        ApplicativeLayerFunctorTests[WriterTC[Option, String]#l, Option].applicativeLayerFunctor[Boolean, Boolean])
      checkAll("ApplicativeLayerFunctor[WriterT[Option, String, ?], Option]",
        SerializableTests.serializable(applicativeLayerFunctor))
    }

    {
      implicit val functorLayerFunctor: FunctorLayerFunctor[WriterTC[Option, String]#l, Option] =
        cats.mtl.instances.writert.writerFunctorLayerFunctor[Option, String]
      checkAll("WriterT[Option, String, ?]",
        FunctorLayerFunctorTests[WriterTC[Option, String]#l, Option].functorLayerFunctor[Boolean])
      checkAll("FunctorLayerFunctor[WriterT[Option, String, ?], Option]",
        SerializableTests.serializable(functorLayerFunctor))
    }
  }
}
