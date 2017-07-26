package cats
package mtl
package tests

final class Syntax extends BaseSuite {

  // test instances.all._
  //noinspection ScalaUnusedSymbol
  {
    import cats.instances.all._
    import cats.mtl.implicits._
    import cats.data._
    test("ApplicativeAsk") {
      ((i: Int) => "$" + i.toString).reader[ReaderIntId]
    }
    test("FunctorListen") {
      val lift = WriterT.lift[Option, String, Int](Option.empty[Int])
      val listen: WriterT[Option, String, (Int, String)] = lift.listen
      val listens: WriterT[Option, String, (Int, String)] = lift.listens((_: String) + "suffix")
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
    test("FunctorEmpty") {
      def operateFunctorEmpty[F[_]: FunctorEmpty](fi: F[Int]): Unit = {
        val _1 = fi.collect { case i if i < 2 => i }
        val _2 = fi.filter(_ < 2)
        val _3 = fi.mapFilter(Some(_).filter(_ < 2))
        ()
      }
      val fa: Map[Int, Int] = Map(1 -> 1, 2 -> 3)
      operateFunctorEmpty(fa)
    }
    test("TraverseEmpty") {
      def operateTraverseEmpty[F[_]: TraverseEmpty](fi: F[Int]): Unit = {
        val _1 = fi.filterA[Option](i => Some(i < 2))
        val _2 = fi.traverseFilter[Option, Int] { i => Some(Some(i).filter(_ < 2)) }
        ()
      }
      val fa: Map[Int, Int] = Map(1 -> 1, 2 -> 3)
      operateTraverseEmpty(fa)
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
