package cats.mtl.tests

import cats.data.Writer
import cats.kernel.Eq
import cats.laws.discipline.ContravariantTests
import cats.laws.discipline.InvariantTests
import cats.laws.discipline.arbitrary._
import cats.mtl.Listen
import cats.mtl.Tell
import org.scalacheck.Arbitrary

class MtlCatsInstanceTests extends BaseSuite {

  implicit def functionEq[A: Arbitrary, B: Eq]: Eq[A => B] = tweakableCatsLawsEqForFn1(100)

  implicit def arbTell[F[_], E](implicit tell: Tell[F, E]): Arbitrary[Tell[F, E]] =
    Arbitrary(tell)

  implicit def arbListen[F[_], E](implicit listen: Listen[F, E]): Arbitrary[Listen[F, E]] =
    Arbitrary(listen)

  checkAll(
    "Contravariant[Tell[Writer[Int, *], Int]]",
    ContravariantTests[Tell[Writer[Int, *], *]].contravariant[Int, String, Int])

  implicit def catsEqForListen[F[_], A](
      implicit eqTell: Eq[A => F[Unit]],
      eqListen: Eq[F[Int] => F[(Int, A)]]): Eq[Listen[F, A]] = Listen.catsEqForListen[F, A, Int]

  checkAll(
    "Invariant[Listen[Writer[Int, *], Int]]",
    InvariantTests[Listen[Writer[Int, *], *]].invariant[Int, Int, Int])
}
