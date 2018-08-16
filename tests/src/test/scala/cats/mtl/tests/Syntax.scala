package cats
package mtl
package tests


final class Syntax extends BaseSuite {

  // test instances.all._
  //noinspection ScalaUnusedSymbol
  {
    import scala.collection.immutable.SortedMap
    import cats.instances.all._
    import cats.mtl.implicits._
    import cats.data._
    import cats.syntax.functor._
    import cats.syntax.either._
    test("ApplicativeAsk") {
      ((i: Int) => "$" + i.toString).reader[ReaderIntId]
      val askedC: Id[Int] = ApplicativeAsk.ask[ReaderIntId, Int].run(1)
      val askedFC: Id[Int] = ApplicativeAsk.askF[ReaderIntId]().run(1)
      val readerFEC: Id[String] = ApplicativeAsk.readerFE[ReaderIntId, Int](i => "$" + i.toString).run(1)
      val readerC: Id[String] = ApplicativeAsk.reader[ReaderIntId, Int, String](i => "$" + i.toString).run(1)
    }
    test("FunctorListen") {
      val lift: WriterT[Option, String, Int] = WriterT.liftF[Option, String, Int](Option.empty[Int])
      val listen: WriterT[Option, String, (Int, String)] = lift.listen
      val listens: WriterT[Option, String, (Int, String)] = lift.listens((_: String) + "suffix")
      val listenC: WriterT[Option, String, (Int, String)] = FunctorListen.listen(lift)
      val listensC: WriterT[Option, String, (Int, String)] = FunctorListen.listens(lift)((_: String) + "suffix")
    }
    test("ApplicativeLocal") {
      val fa: OptionT[FunctionC[String]#l, Int] = OptionT.liftF[FunctionC[String]#l, Int](_.length)
      val local = fa.local[String](s => s + "!").value("ha")
      val scope = fa.scope[String]("state").value("ha")
      val localC: String = ApplicativeLocal.local((s: String) => s + "!")(ApplicativeAsk.askF[FunctionC[String]#l]()).apply("ha")
      val scopeC: Option[Int] = ApplicativeLocal.scope("state")(fa).value.apply("ha")
    }
    test("FunctorRaise") {
      val fa: Either[String, Int] = "ha".raise[EitherC[String]#l, Int]
      val fat: EitherT[Option, String, Int] = "ha".raise[EitherTC[Option, String]#l, Int]
      val faC: Either[String, Int] = FunctorRaise.raise[EitherC[String]#l, String, Int]("ha")
      val faeC: Either[String, Nothing] = FunctorRaise.raiseF[EitherC[String]#l]("ha")
    }
    test("ApplicativeHandle") {
      val fa: Option[Int] = Option.empty[Int].handle((_: Unit) => 42)
      val fb: Option[Int] = Option.empty[Int].handleWith((_: Unit) => Some(22))
    }
    test("FunctorEmpty") {
      def operateFunctorEmpty[F[_]: Functor: FunctorEmpty](fi: F[Int]): Unit = {
        val _1 = fi.collect { case i if i < 2 => i }
        val _2 = fi.filter(_ < 2)
        val _3 = fi.mapFilter(Some(_).filter(_ < 2))
        val _4 = fi.map(a => Some(a): Option[Int]).flattenOption
        ()
      }
      val fa: Map[Int, Int] = Map(1 -> 1, 2 -> 3)
      operateFunctorEmpty(fa)

      val sortedFa: SortedMap[Int, Int] = SortedMap(1 -> 1, 2 -> 3)
      operateFunctorEmpty(sortedFa)
    }
    test("TraverseEmpty") {
      def operateTraverseEmpty[F[_]: TraverseEmpty](fi: F[Int]): Unit = {
        val _1 = fi.filterA[Option](i => Some(i < 2))
        val _2 = fi.traverseFilter[Option, Int] { i => Some(Some(i).filter(_ < 2)) }
        ()
      }
      val fa: SortedMap[Int, Int] = SortedMap(1 -> 1, 2 -> 3)
      operateTraverseEmpty(fa)
    }
    test("MonadState") {
      val mod: Eval[(String, Unit)] = ((s: String) => s + "!").modify[StateC[String]#l].run("")
      val set: Eval[(String, Unit)] = "ha".set[StateC[String]#l].run("")
      val getC: Eval[(String, String)] = MonadState.get[StateC[String]#l, String].run("")
      val setFC: Eval[(String, Unit)] = MonadState.setF[StateC[String]#l]("ha").run("")
      val setC: Eval[(String, Unit)] = MonadState.set[StateC[String]#l, String]("ha").run("")
      val modC: State[String, Unit] = MonadState.modify[StateC[String]#l, String]((s: String) => s + "!")
      val inspectC: State[String, String] = MonadState.inspect[StateC[String]#l, String, String]((s: String) => s + "!")
    }
    test("FunctorTell") {
      val told: WriterT[Option, String, Unit] = "ha".tell[WriterTC[Option, String]#l]
      val tupled: WriterT[Option, String, Unit] = ("ha", ()).tuple[WriterTC[Option, String]#l]
      val toldC = FunctorTell.tell[WriterTC[Option, String]#l, String]("ha")
      val toldFC = FunctorTell.tellF[WriterTC[Option, String]#l]("ha")
    }
  }

}
