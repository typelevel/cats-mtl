package cats
package mtl
package tests

final class Syntax extends BaseSuite {

  // test instances.all._
  //noinspection ScalaUnusedSymbol
  {
    import cats.instances.all._
    import cats.mtl.instances.all._
    import cats.mtl.syntax.all._
    import cats.data._
    test("ApplicativeAsk") {
      ((i: Int) => "$" + i.toString).reader[ReaderIntId]
    }
    test("FunctorListen") {
      val lift = WriterT.lift[Option, String, Int](Option.empty[Int])
      val listen: WriterT[Option, String, (Int, String)] = lift.listen
      val listens: WriterT[Option, String, (Int, String)] = lift.listens((_: String) + "suffix")
      val pass: WriterT[Option, String, Int] =
        WriterT.lift[Option, String, (Int, String => String)](Option.empty[(Int, String => String)]).pass
    }
    test("ApplicativeLocal") {
      val fa: OptionT[FunctionC[String]#l, Int] = OptionT.pure(1)
      val y = fa.local[String](s => s + "!")
      val z = fa.scope[String]("state")
    }
    test("FunctorRaise") {
      val fa: Either[String, Int] = "ha".raise[EitherC[String]#l, Int]
      val fat: EitherT[Option, String, Int] = "ha".raise[EitherTC[Option, String]#l, Int]
    }
    test("MonadState") {
      val mod: State[String, Unit] = ((s: String) => s + "!").modify[StateC[String]#l]
      val set: State[String, Unit] = "ha".set[StateC[String]#l]
    }
    test("FunctorTell") {
      val told: WriterT[Option, String, Unit] = "ha".tell[WriterTC[Option, String]#l]
    }
  }

}
