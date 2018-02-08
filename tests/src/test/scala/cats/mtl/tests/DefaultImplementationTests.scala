package cats.mtl.tests

import cats.data.{State, StateT}
import cats.{Applicative, Eval, Functor, Monad, Traverse}
import cats.mtl._
import cats.mtl.laws.discipline._
import cats.implicits._
import cats.laws.discipline.arbitrary._
import cats.laws.discipline.eq._

class FunctorEmptyDefaultTests extends BaseSuite {
  val defaultListFunctorEmpty: FunctorEmpty[List] = new DefaultFunctorEmpty[List] {
    def mapFilter[A, B](fa: List[A])(f: A => Option[B]): List[B] = fa.collect(Function.unlift(f))
    val functor: Functor[List] = implicitly

  }

  checkAll("List",
    FunctorEmptyTests[List](defaultListFunctorEmpty)
      .functorEmpty[String, String, String])
}

class TraverseEmptyDefaultTests extends BaseSuite {
  val defaultListTraverseEmpty: TraverseEmpty[List] = new DefaultTraverseEmpty[List] {
    val traverse: Traverse[List] = implicitly
    val functorEmpty: FunctorEmpty[List] = cats.mtl.instances.empty.listTraverseEmpty.functorEmpty

    def traverseFilter[G[_] : Applicative, A, B](fa: List[A])(f: A => G[Option[B]]): G[List[B]] =
      fa.foldRight(List.empty[B].pure[G].pure[Eval])(
        (x, xse) =>
          xse.map(xs =>
            Applicative[G].product(f(x), xs).map { case (i, o) => i.fold(o)(_ :: o) }
          )
      ).value
  }

  checkAll("List",
    TraverseEmptyTests[List](defaultListTraverseEmpty)
      .traverseEmpty[String, String, String])
}


class FunctorTellDefaultTests extends StateTTestsBase {
  val defaultListFunctorTell: FunctorTell[StateTC[Option, String]#l, String] = new DefaultFunctorTell[StateTC[Option, String]#l, String] {

    val functor: Functor[StateTC[Option, String]#l] = implicitly

    def tell(l: String): StateT[Option, String, Unit] = StateT.set(l)
  }

  checkAll("StateT[Option, String, ?]",
    FunctorTellTests[StateTC[Option, String]#l, String](defaultListFunctorTell)
      .functorTell[String])
}

class MonadStateDefaultTests extends StateTTestsBase {
  val defaultListMonadState: MonadState[StateC[String]#l, String] = new DefaultMonadState[StateC[String]#l, String] {

    val monad: Monad[StateC[String]#l] = implicitly

    def get: StateC[String]#l[String] = State.get

    def set(s: String): State[String, Unit] = State.set(s)
  }

  checkAll("State[String, ?]",
    MonadStateTests[StateC[String]#l, String](defaultListMonadState)
      .monadState[Int])
}


class ApplicativeAskDefaultTests extends BaseSuite {

  val defaultApplicativeAsk: ApplicativeAsk[FunctionC[Int]#l, Int] = new DefaultApplicativeAsk[FunctionC[Int]#l, Int] {

    val applicative: Applicative[FunctionC[Int]#l] = implicitly

    def ask: FunctionC[Int]#l[Int] = identity
  }

  checkAll("FunctionC[Int]#l[Int]",
    ApplicativeAskTests[FunctionC[Int]#l, Int](defaultApplicativeAsk)
      .applicativeAsk[String])
}


class ApplicativeLocalDefaultTests extends BaseSuite {

  val defaultApplicativeLocal: ApplicativeLocal[FunctionC[Int]#l, Int] = new DefaultApplicativeLocal[FunctionC[Int]#l, Int] {

    val ask: ApplicativeAsk[FunctionC[Int]#l, Int] = cats.mtl.instances.local.askFunction

    def local[A](f: Int => Int)(fa: FunctionC[Int]#l[A]): FunctionC[Int]#l[A] = fa compose f
  }

  checkAll("FunctionC[Int]#l[Int]",
    ApplicativeLocalTests[FunctionC[Int]#l, Int](defaultApplicativeLocal)
      .applicativeLocal[String, String])
}
