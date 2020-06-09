/*
 * Copyright 2020 Typelevel
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

final class Syntax extends BaseSuite {

  sealed trait Foo
  case class Bar(n: Int) extends Foo

  // test instances.all._
  //noinspection ScalaUnusedSymbol
  {
    import cats.instances.all._
    import cats.mtl.implicits._
    import cats.data._
    test("Ask") {
      ((i: Int) => "$" + i.toString).reader[ReaderIntId]
      val askedC: Id[Int] = Ask.ask[ReaderIntId, Int].run(1)
      val askedFC: Id[Int] = Ask.askF[ReaderIntId]().run(1)
      val readerFEC: Id[String] =
        Ask.readerFE[ReaderIntId, Int](i => "$" + i.toString).run(1)
      val readerC: Id[String] =
        Ask.reader[ReaderIntId, Int, String](i => "$" + i.toString).run(1)

      def foo[F[_]](implicit F: Ask[F, Foo]) =
        F.ask

      def bar[F[_]](implicit F: Ask[F, Bar]) =
        foo[F]
    }
    test("Listen") {
      val lift: WriterT[Option, String, Int] =
        WriterT.liftF[Option, String, Int](Option.empty[Int])
      val listen: WriterT[Option, String, (Int, String)] = lift.listen
      val listens: WriterT[Option, String, (Int, String)] = lift.listens((_: String) + "suffix")
      val listenC: WriterT[Option, String, (Int, String)] = Listen.listen(lift)
      val listensC: WriterT[Option, String, (Int, String)] =
        Listen.listens(lift)((_: String) + "suffix")
    }
    test("Local") {
      val fa: OptionT[Reader[String, *], Int] =
        OptionT.liftF[Reader[String, *], Int](Reader(_.length))
      val local = fa.local[String](s => s + "!").value("ha")
      val scope = fa.scope[String]("state").value("ha")
      val localC: String =
        Local.local(Ask.askF[Reader[String, *]]())((s: String) => s + "!").apply("ha")
      val scopeC: Option[Int] = Local.scope(fa)("state").value.apply("ha")
    }
    test("Raise") {
      val fa: Either[String, Int] = "ha".raise[EitherC[String]#l, Int]
      val fat: EitherT[Option, String, Int] = "ha".raise[EitherTC[Option, String]#l, Int]
      def fb[F[_]: Raise[?[_], EE], E <: EE, EE](e: E): F[E] = e.raise[F, E]
      val faC: Either[String, Int] = Raise.raise[EitherC[String]#l, String, Int]("ha")
      val faeC: Either[String, Nothing] = Raise.raiseF[EitherC[String]#l]("ha")

      def bar[F[_]](implicit F: Raise[F, Bar]) =
        F.raise(Bar(404))

      def foo[F[_]](implicit F: Raise[F, Foo]): F[Nothing] =
        bar[F]
    }
    test("Handle") {
      val fa: Option[Either[Unit, Int]] = Option.empty[Int].attemptHandle
      val fb: EitherT[Option, Unit, Int] = Option.empty[Int].attemptHandleT
      val fc: Option[Int] = Option.empty[Int].handle((_: Unit) => 42)
      val fd: Option[Int] = Option.empty[Int].handleWith((_: Unit) => Some(22))
    }
    test("Stateful") {
      val mod: Eval[(String, Unit)] = ((s: String) => s + "!").modify[StateC[String]#l].run("")
      val set: Eval[(String, Unit)] = "ha".set[StateC[String]#l].run("")
      val getC: Eval[(String, String)] = Stateful.get[StateC[String]#l, String].run("")
      val setFC: Eval[(String, Unit)] = Stateful.setF[StateC[String]#l]("ha").run("")
      val setC: Eval[(String, Unit)] = Stateful.set[StateC[String]#l, String]("ha").run("")
      val modC: State[String, Unit] =
        Stateful.modify[StateC[String]#l, String]((s: String) => s + "!")
      val inspectC: State[String, String] =
        Stateful.inspect[StateC[String]#l, String, String]((s: String) => s + "!")
    }
    test("Tell") {
      val told: WriterT[Option, String, Unit] = "ha".tell[WriterTC[Option, String]#l]
      val tupled: WriterT[Option, String, Unit] = ("ha", ()).tuple[WriterTC[Option, String]#l]
      val toldC = Tell.tell[WriterTC[Option, String]#l, String]("ha")
      val toldFC = Tell.tellF[WriterTC[Option, String]#l]("ha")

      def bar[F[_]](implicit F: Tell[F, Bar]) =
        F.tell(Bar(404))

      def foo[F[_]](implicit F: Tell[F, Foo]): F[Unit] =
        bar[F]
    }
    test("Chronicle") {
      val chronicleC: Ior[Int, String] =
        Chronicle.chronicle[IorC[Int]#l, Int, String](Ior.right[Int, String]("w00t"))
      val confessC: Ior[String, Int] =
        Chronicle.confess[IorC[String]#l, String, Int]("error")
      val discloseTC: IorT[Option, String, String] =
        Chronicle.disclose[IorTC[Option, String]#l, String, String]("w00t")
      val dictateC: Ior[Int, Unit] = Chronicle.dictate[IorC[Int]#l, Int](42)
      val materializeTC: IorT[Option, String, Ior[String, Int]] =
        Chronicle.materialize[IorTC[Option, String]#l, String, Int](
          IorT.pure[Option, String](42))

      val fa: IorT[Option, String, Int] = IorT.pure(42)
      val dictate: IorT[Option, String, Unit] = "err".dictate[IorTC[Option, String]#l]
      val disclose: IorT[Option, String, Int] = "err".disclose[IorTC[Option, String]#l, Int]
      val confess: IorT[Option, String, Int] = "err".confess[IorTC[Option, String]#l, Int]
      val memento: IorT[Option, String, Either[String, Int]] = fa.memento
      val absolve: IorT[Option, String, Int] = fa.absolve(42)
      val condemn: IorT[Option, String, Int] = fa.condemn
      val retcon: IorT[Option, String, Int] = fa.retcon((str: String) => str + "err")
      val chronicle: IorT[Option, String, Int] =
        Ior.both("hello", 42).chronicle[IorTC[Option, String]#l]
    }
  }
}
