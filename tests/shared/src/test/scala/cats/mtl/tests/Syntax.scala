/*
 * Copyright 2021 Typelevel
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cats
package mtl
package tests

import cats.syntax.semigroup._ // TODO we can't do all here because MTL syntax conflicts

final class Syntax extends BaseSuite {

  sealed trait Foo
  case class Bar(n: Int) extends Foo

  // test instances.all._
  // noinspection ScalaUnusedSymbol
  {
    import cats.syntax.apply._
    import cats.mtl.implicits._
    import cats.data._
    test("Ask") {
      ((i: Int) => "$" + i.toString).reader[ReaderIntId]
      Ask.ask[ReaderIntId, Int].run(1): Id[Int]
      Ask.askF[ReaderIntId]().run(1): Id[Int]
      Ask.readerFE[ReaderIntId, Int](i => "$" + i.toString).run(1): Id[String]
      Ask.reader[ReaderIntId, Int, String](i => "$" + i.toString).run(1): Id[String]
    }

    test("Listen") {
      val lift: WriterT[Option, String, Int] =
        WriterT.liftF[Option, String, Int](Option.empty[Int])
      lift.listen: WriterT[Option, String, (Int, String)]
      lift.listens((_: String) + "suffix"): WriterT[Option, String, (Int, String)]
      Listen.listen(lift): WriterT[Option, String, (Int, String)]
      Listen.listens(lift)((_: String) + "suffix"): WriterT[Option, String, (Int, String)]
    }

    test("Local") {
      val fa: OptionT[Reader[String, *], Int] =
        OptionT.liftF[Reader[String, *], Int](Reader(_.length))
      fa.local[String](s => s + "!").value("ha")
      fa.scope[String]("state").value("ha")
      Local.local(Ask.askF[Reader[String, *]]())((s: String) => s + "!").apply("ha"): String
      Local.scope(fa)("state").value.apply("ha"): Option[Int]
    }

    test("Raise") {
      "ha".raise[EitherC[String]#l, Int]: Either[String, Int]
      "ha".raise[EitherTC[Option, String]#l, Int]: EitherT[Option, String, Int]
      Raise.raise[EitherC[String]#l, String, Int]("ha"): Either[String, Int]
      Raise.raiseF[EitherC[String]#l]("ha"): Either[String, Nothing]

      def bar[F[_]](implicit F: Raise[F, Bar]): F[Unit] =
        Bar(404).raise

      def foo[F[_]: Apply](implicit F: Raise[F, Foo]): F[Int] =
        bar[F] *> Bar(42).raise

      val _ = foo[Either[Foo, *]]

      def fooWithApplicativeError[F[_]](implicit
          // `Applicative` is required by `liftTo`.
          // `ApplicativeError` is used to enforce checks for non-ambiguity.
          F: ApplicativeError[F, Foo],
          raise: Raise[F, Foo]): Unit = {

        val _ = (
          Some("abc").liftTo[F](Bar(123)): F[String],
          Some(Bar(456)).raiseTo[F]: F[Unit]
        )
      }

      fooWithApplicativeError[Either[Foo, *]]
    }

    test("Handle") {
      Option.empty[Int].attemptHandle: Option[Either[Unit, Int]]
      Option.empty[Int].attemptHandleT: EitherT[Option, Unit, Int]
      Option.empty[Int].handle((_: Unit) => 42): Option[Int]
      Option.empty[Int].handleWith((_: Unit) => Some(22)): Option[Int]
    }

    test("Stateful") {
      ((s: String) => s + "!").modify[StateC[String]#l].run(""): Eval[(String, Unit)]
      "ha".set[StateC[String]#l].run(""): Eval[(String, Unit)]
      Stateful.get[StateC[String]#l, String].run(""): Eval[(String, String)]
      Stateful.setF[StateC[String]#l]("ha").run(""): Eval[(String, Unit)]
      Stateful.set[StateC[String]#l, String]("ha").run(""): Eval[(String, Unit)]
      Stateful.modify[StateC[String]#l, String]((s: String) => s + "!"): State[String, Unit]
      Stateful.inspect[StateC[String]#l, String, String]((s: String) => s + "!"): State[
        String,
        String]
    }

    test("Tell") {
      "ha".tell[WriterTC[Option, String]#l]: WriterT[Option, String, Unit]
      ("ha", ()).tuple[WriterTC[Option, String]#l]: WriterT[Option, String, Unit]
      Tell.tell[WriterTC[Option, String]#l, String]("ha")
      Tell.tellF[WriterTC[Option, String]#l]("ha")

      def bar[F[_]](implicit F: Tell[F, Bar]): F[Unit] =
        Bar(404).tell

      def foo[F[_]: Apply](implicit F: Tell[F, Foo]): F[Unit] =
        bar[F] *> Bar(42).tell

      implicit val mf: Monoid[Foo] =
        Monoid.instance(Bar(Monoid.empty[Int]), { case (Bar(a), Bar(b)) => Bar(a |+| b) })

      val _ = foo[WriterT[Option, Foo, *]]
    }

    test("Chronicle") {
      Chronicle
        .chronicle[IorC[Int]#l, Int, String](Ior.right[Int, String]("w00t")): Ior[Int, String]
      Chronicle.confess[IorC[String]#l, String, Int]("error"): Ior[String, Int]
      Chronicle
        .disclose[IorTC[Option, String]#l, String, String]("w00t"): IorT[Option, String, String]
      Chronicle.dictate[IorC[Int]#l, Int](42): Ior[Int, Unit]
      Chronicle.materialize[IorTC[Option, String]#l, String, Int](
        IorT.pure[Option, String](42)): IorT[Option, String, Ior[String, Int]]

      val fa: IorT[Option, String, Int] = IorT.pure(42)

      "err".dictate[IorTC[Option, String]#l]: IorT[Option, String, Unit]
      "err".disclose[IorTC[Option, String]#l, Int]: IorT[Option, String, Int]
      "err".confess[IorTC[Option, String]#l, Int]: IorT[Option, String, Int]
      fa.memento: IorT[Option, String, Either[String, Int]]
      fa.absolve(42): IorT[Option, String, Int]
      fa.condemn: IorT[Option, String, Int]
      fa.retcon((str: String) => str + "err"): IorT[Option, String, Int]
      Ior.both("hello", 42).chronicle[IorTC[Option, String]#l]: IorT[Option, String, Int]
    }
  }
}
