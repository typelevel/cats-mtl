package cats.mtl.tests

import cats.data.{State, StateT}
import cats.{Applicative, Eval, Functor, Monad, Monoid, Traverse}
import cats.mtl._
import cats.mtl.laws.discipline._
import cats.implicits._
import cats.laws.discipline.arbitrary._
import cats.laws.discipline.eq._


class FunctorTellDefaultTests extends StateTTestsBase {
  val defaultListFunctorTell: FunctorTell[StateTC[Option, String]#l, String] = new DefaultFunctorTell[StateTC[Option, String]#l, String] {

    val functor: Functor[StateTC[Option, String]#l] = implicitly

    def tell(l: String): StateT[Option, String, Unit] = StateT.set(l)
  }

  checkAll("StateT[Option, String, ?]",
    FunctorTellTests[StateTC[Option, String]#l, String](defaultListFunctorTell)
      .functorTell[String])
}

class ApplicativeCensorDefaultTests extends StateTTestsBase {
  val defaultTupleApplicativeCensor: ApplicativeCensor[(String, ?), String] =
    new DefaultApplicativeCensor[(String, ?), String] {
      val applicative: Applicative[Tuple2[String, ?]] = implicitly

      val monoid: Monoid[String] = implicitly

      def censor[A](fa: (String, A))(f: String => String): (String, A) =
        (f(fa._1), fa._2)

      def tell(l: String): (String, Unit) = (l, ())

      def listen[A](fa: (String, A)): (String, (A, String)) = (fa._1, fa.swap)
    }

  checkAll("(String, ?)",
    ApplicativeCensorTests[(String, ?), String](defaultTupleApplicativeCensor)
      .applicativeCensor[String, Int])
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

  val pureApplicativeAsk: ApplicativeAsk[FunctionC[Int]#l, Int] = ApplicativeAsk.const(42)

  checkAll("DefaultApplicativeAsk FunctionC[Int]#l[Int]",
    ApplicativeAskTests[FunctionC[Int]#l, Int](defaultApplicativeAsk)
      .applicativeAsk[String])

  checkAll("ApplicativeAsk.const FunctionC[Int]#l[Int]",
    ApplicativeAskTests[FunctionC[Int]#l, Int](pureApplicativeAsk)
      .applicativeAsk[String])
}


class ApplicativeLocalDefaultTests extends BaseSuite {

  val defaultApplicativeLocal: ApplicativeLocal[FunctionC[Int]#l, Int] = new DefaultApplicativeLocal[FunctionC[Int]#l, Int] {

    val applicative: Applicative[FunctionC[Int]#l] = cats.instances.function.catsStdMonadForFunction1[Int]

    def ask: Int => Int = identity[Int]

    def local[A](f: Int => Int)(fa: FunctionC[Int]#l[A]): FunctionC[Int]#l[A] = fa compose f
  }

  checkAll("FunctionC[Int]#l[Int]",
    ApplicativeLocalTests[FunctionC[Int]#l, Int](defaultApplicativeLocal)
      .applicativeLocal[String, String])
}
