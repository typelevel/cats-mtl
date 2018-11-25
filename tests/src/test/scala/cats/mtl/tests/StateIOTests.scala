package cats
package mtl
package tests


import cats.implicits._
import cats.laws.discipline.arbitrary._
import cats.laws.discipline._
import cats.effect.IO
import cats.effect.concurrent.Deferred
import cats.effect.laws.discipline.arbitrary._
import cats.effect.laws.util.TestInstances._
import cats.effect.laws.util.TestContext
import cats.laws.discipline.eq._
import cats.mtl.laws.discipline.MonadStateTests
import cats.mtl.special.StateIO
import org.scalacheck._

class StateIOTests extends BaseSuite {

  implicit val ec = TestContext()

  implicit def arbStateIO[S: Arbitrary, A: Arbitrary: Cogen]: Arbitrary[StateIO[S, A]] =
    Arbitrary(Gen.oneOf(
      catsEffectLawsArbitraryForIO[A].arbitrary.map(StateIO.liftF[S, A]),
      implicitly[Arbitrary[A]].arbitrary.map(a => StateIO.get[S].as(a)),
      implicitly[Arbitrary[A]].arbitrary.flatMap(a =>
        implicitly[Arbitrary[S]].arbitrary.map(s => StateIO.set(s).as(a))),
      implicitly[Arbitrary[A]].arbitrary.flatMap(a =>
        implicitly[Arbitrary[S]].arbitrary.map(s => StateIO.modify[S](_ => s).as(a)))
    ))


  implicit def eqStateIO[S: Arbitrary: Eq, A: Eq](implicit ctx: TestContext, e: Eq[IO[(S, A)]]): Eq[StateIO[S, A]] =
    Eq.by(sio => { (s: S) =>
      Deferred.uncancelable[IO, (S, A)].flatMap(deferred =>
        IO(sio.unsafeRunAsync(s) {
          case Right(sa) => deferred.complete(sa).unsafeRunSync()
          case Left(t) => throw t
        }).flatMap(_ => deferred.get))
    })

  checkAll("StateIO[String, Int]",
    MonadStateTests[StateIO[String, ?], String]
      .monadState[Int])
  checkAll("MonadState[StateIO[String, ?]]",
    SerializableTests.serializable(MonadState[StateIO[String, ?], String]))

}
