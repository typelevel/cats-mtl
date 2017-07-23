package cats
package mtl
package tests

import cats.arrow.FunctionK
import cats.data._
import cats.instances.all._
import cats.laws.discipline.SerializableTests
import cats.laws.discipline.arbitrary._
import cats.laws.discipline.eq._
import cats.mtl.instances.local._
import cats.mtl.laws.discipline._
import org.scalacheck._

class ReaderTTests extends BaseSuite {
  implicit val arbFunctionK: Arbitrary[Option ~> Option] =
    Arbitrary(Gen.oneOf(new (Option ~> Option) {
      def apply[A](fa: Option[A]): Option[A] = None
    }, FunctionK.id[Option]))

  implicit def eqKleisli[F[_], A, B](implicit arb: Arbitrary[A], ev: Eq[F[B]]): Eq[Kleisli[F, A, B]] = {
    Eq.by((x: (Kleisli[F, A, B])) => x.run)
  }

  implicit def eqKleisliId[A, B](implicit arb: Arbitrary[A], ev: Eq[B]): Eq[Kleisli[Id, A, B]] = {
    eqKleisli[Id, A, B]
  }

  implicit def catsLawsArbitraryForKleisliId[A, B](implicit F: Arbitrary[A => B]): Arbitrary[Kleisli[Id, A, B]] = {
    Arbitrary(F.arbitrary.map(Kleisli[Id, A, B]))
  }

  implicit def catsLawArbitraryForStateT[F[_], S, A](implicit F: Arbitrary[F[S => F[(S, A)]]]): Arbitrary[StateT[F, S, A]] = {
    Arbitrary(F.arbitrary.map(StateT.applyF))
  }

  implicit def stateTEq[F[_], S, A](implicit S: Arbitrary[S], FSA: Eq[F[(S, A)]], F: FlatMap[F]): Eq[StateT[F, S, A]] = {
    Eq.by[StateT[F, S, A], S => F[(S, A)]](state =>
      s => state.run(s))
  }

  {
    implicit val monadLayerControl: MonadLayerControl.Aux[ReaderTC[Option, String]#l, Option, Id] =
      cats.mtl.instances.readert.readerMonadLayerControl[Option, String]
    checkAll("ReaderT[Option, String, ?]",
      MonadLayerControlTests[ReaderTC[Option, String]#l, Option, Id]
        .monadLayerControl[Boolean, Boolean])
    checkAll("MonadLayerControl[ReaderT[Option, String, ?], Option]",
      SerializableTests.serializable(monadLayerControl))
  }

  {
    implicit val applicativeLayerFunctor: ApplicativeLayerFunctor[ReaderTC[Option, String]#l, Option] =
      cats.mtl.instances.readert.readerApplicativeLayerFunctor[Option, String]
    checkAll("ReaderT[Option, String, ?]",
      ApplicativeLayerFunctorTests[ReaderTC[Option, String]#l, Option]
        .applicativeLayerFunctor[Boolean, Boolean])
    checkAll("ApplicativeLayerFunctor[ReaderT[Option, String, ?], Option]",
      SerializableTests.serializable(applicativeLayerFunctor))
  }

  {
    implicit val functorLayerFunctor: FunctorLayerFunctor[ReaderTC[Option, String]#l, Option] =
      cats.mtl.instances.readert.readerFunctorLayerFunctor[Option, String]
    checkAll("ReaderT[Option, String, ?]",
      FunctorLayerFunctorTests[ReaderTC[Option, String]#l, Option]
        .functorLayerFunctor[Boolean])
    checkAll("FunctorLayerFunctor[ReaderT[Option, String, ?], Option]",
      SerializableTests.serializable(functorLayerFunctor))
  }

  {
    checkAll("Reader[String, ?]",
      ApplicativeLocalTests[ReaderTC[Id, String]#l, String]
        .applicativeLocal[String])
    checkAll("FunctorLocal[Reader[String, ?], String]",
      SerializableTests.serializable(ApplicativeLocal[ReaderTC[Id, String]#l, String]))
  }

  {
    checkAll("ReaderT[Option, String, ?]",
      ApplicativeLocalTests[ReaderTC[Option, String]#l, String]
        .applicativeLocal[String])
    checkAll("FunctorLocal[ReaderT[Option, String, ?], String]",
      SerializableTests.serializable(ApplicativeLocal[ReaderTC[Option, String]#l, String]))
  }

  {
    import mtl.instances.readert.readerMonadLayerControl
    import mtl.instances.eithert.eitherMonadLayerControl
    import mtl.instances.statet.stateMonadLayerControl
    import mtl.instances.optiont.optionMonadLayerControl
    import mtl.instances.writert.writerMonadLayerControl

    {
      implicit def slowCatsLawsEqForFn1[A, B](implicit A: Arbitrary[A], B: Eq[B]): Eq[A => B] =
        tweakableCatsLawsEqForFn1[A, B](20)

      checkAll("ReaderT[ReaderT[Option, String, ?], Int, ?]",
        ApplicativeLocalTests[ReaderTIntOverReaderTStringOverOption, String]
          .applicativeLocal[String])
      checkAll("FunctorLocal[ReaderT[ReaderT[Option, String, ?], Int, ?], String]",
        SerializableTests.serializable(ApplicativeLocal[ReaderTIntOverReaderTStringOverOption, String]))

      checkAll("StateT[ReaderT[Option, String, ?], Int, ?]",
        ApplicativeLocalTests[StateTIntOverReaderTStringOverOption, String]
          .applicativeLocal[String])
      checkAll("FunctorLocal[StateT[ReaderT[Option, String, ?], Int, ?], String]",
        SerializableTests.serializable(ApplicativeLocal[StateTIntOverReaderTStringOverOption, String]))
    }

    checkAll("WriterT[ReaderT[Option, String, ?], Int, ?]",
      ApplicativeLocalTests[WriterTIntOverReaderTStringOverOption, String]
        .applicativeLocal[String])
    checkAll("FunctorLocal[WriterT[ReaderT[Option, String, ?], Int, ?], String]",
      SerializableTests.serializable(ApplicativeLocal[WriterTIntOverReaderTStringOverOption, String]))

    checkAll("OptionT[ReaderT[Option, String, ?], ?]",
      ApplicativeLocalTests[OptionTOverReaderTStringOverOption, String]
        .applicativeLocal[String])
    checkAll("FunctorLocal[OptionT[ReaderT[Option, String, ?], ?], String]",
      SerializableTests.serializable(ApplicativeLocal[OptionTOverReaderTStringOverOption, String]))

    checkAll("EitherT[ReaderT[Option, String, ?], String, ?]",
      ApplicativeLocalTests[EitherTIntOverReaderTStringOverOption, String]
        .applicativeLocal[String])
    checkAll("FunctorLocal[EitherT[ReaderT[Option, String, ?], Int, ?], String]",
      SerializableTests.serializable(ApplicativeLocal[EitherTIntOverReaderTStringOverOption, String]))

  }

}
