package cats
package mtl
package tests

import cats.arrow.FunctionK
import cats.data._
import cats.instances.all._
import cats.laws.discipline.SerializableTests
import cats.laws.discipline.arbitrary._
import cats.laws.discipline.eq._
import cats.mtl.laws.discipline._
import org.scalacheck._

class ReaderTTests extends BaseSuite {
  implicit val arbFunctionK: Arbitrary[Option ~> Option] =
    Arbitrary(
      Gen.oneOf(
        new (Option ~> Option) {
          def apply[A](fa: Option[A]): Option[A] = None
        },
        FunctionK.id[Option]))

  implicit def eqKleisli[F[_], A, B](
      implicit arb: Arbitrary[A],
      ev: Eq[F[B]]): Eq[Kleisli[F, A, B]] =
    Eq.by((x: (Kleisli[F, A, B])) => x.run)

  implicit def eqKleisliId[A, B](implicit arb: Arbitrary[A], ev: Eq[B]): Eq[Kleisli[Id, A, B]] =
    eqKleisli[Id, A, B]

  implicit def stateTEq[F[_], S, A](
      implicit S: Arbitrary[S],
      FSA: Eq[F[(S, A)]],
      F: FlatMap[F]): Eq[StateT[F, S, A]] =
    Eq.by[StateT[F, S, A], S => F[(S, A)]](state => s => state.run(s))

  {
    Applicative[Reader[String, *]]
    checkAll(
      "Reader[String, *]",
      ApplicativeLocalTests[Kleisli[Id, String, *], String].applicativeLocal[String, String])
    checkAll(
      "FunctorLocal[Reader[String, *], String]",
      SerializableTests.serializable(ApplicativeLocal[Kleisli[Id, String, *], String]))
  }

  {
    checkAll(
      "ReaderT[Option, String, *]",
      ApplicativeLocalTests[ReaderTC[Option, String]#l, String]
        .applicativeLocal[String, String])
    checkAll(
      "FunctorLocal[ReaderT[Option, String, *], String]",
      SerializableTests.serializable(ApplicativeLocal[ReaderTC[Option, String]#l, String]))
  }

  {

    {
      implicit def slowCatsLawsEqForFn1[A, B](implicit A: Arbitrary[A], B: Eq[B]): Eq[A => B] =
        tweakableCatsLawsEqForFn1[A, B](20)

      checkAll(
        "ReaderT[ReaderT[Option, String, *], Int, *]",
        ApplicativeLocalTests[ReaderTIntOverReaderTStringOverOption, String]
          .applicativeLocal[String, String])
      checkAll(
        "FunctorLocal[ReaderT[ReaderT[Option, String, *], Int, *], String]",
        SerializableTests.serializable(
          ApplicativeLocal[ReaderTIntOverReaderTStringOverOption, String])
      )

      checkAll(
        "StateT[ReaderT[Option, String, *], Int, *]",
        ApplicativeLocalTests[StateTIntOverReaderTStringOverOption, String]
          .applicativeLocal[String, String])
      checkAll(
        "FunctorLocal[StateT[ReaderT[Option, String, *], Int, *], String]",
        SerializableTests.serializable(
          ApplicativeLocal[StateTIntOverReaderTStringOverOption, String])
      )
    }

    checkAll(
      "WriterT[ReaderT[Option, String, *], Int, *]",
      ApplicativeLocalTests[WriterTIntOverReaderTStringOverOption, String]
        .applicativeLocal[String, String])
    checkAll(
      "FunctorLocal[WriterT[ReaderT[Option, String, *], Int, *], String]",
      SerializableTests.serializable(
        ApplicativeLocal[WriterTIntOverReaderTStringOverOption, String])
    )

    checkAll(
      "OptionT[ReaderT[Option, String, *], *]",
      ApplicativeLocalTests[OptionTOverReaderTStringOverOption, String]
        .applicativeLocal[String, String])
    checkAll(
      "FunctorLocal[OptionT[ReaderT[Option, String, *], *], String]",
      SerializableTests.serializable(
        ApplicativeLocal[OptionTOverReaderTStringOverOption, String])
    )

    checkAll(
      "EitherT[ReaderT[Option, String, *], String, *]",
      ApplicativeLocalTests[EitherTIntOverReaderTStringOverOption, String]
        .applicativeLocal[String, String])
    checkAll(
      "FunctorLocal[EitherT[ReaderT[Option, String, *], Int, *], String]",
      SerializableTests.serializable(
        ApplicativeLocal[EitherTIntOverReaderTStringOverOption, String])
    )

  }

}
