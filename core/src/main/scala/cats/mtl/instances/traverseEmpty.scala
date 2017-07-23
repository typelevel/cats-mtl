package cats
package mtl
package instances

object traverseEmpty {

}

trait TraverseEmptyInstances {

  import cats.syntax.all._

  implicit val optionTraverseEmpty: TraverseEmpty[Option] = new TraverseEmpty[Option] {
    override val functorEmpty: FunctorEmpty[Option] = new FunctorEmpty[Option] {
      override val functor: Functor[Option] = cats.instances.option.catsStdInstancesForOption

      override def empty[A]: Option[A] = None

      override def mapFilter[A, B](fa: Option[A])(f: (A) => Option[B])(implicit ev: Monad[Option]): Option[B] = {
        fa.flatMap(f)
      }
    }

    override def traverseFilter[G[_] : Applicative, A, B](fa: Option[A])(f: (A) => G[Option[B]]): G[Option[B]] = {
      fa match {
        case _: None.type => Option.empty[B].pure[G]
        case Some(a) => f(a)
      }
    }

    override def filterA[G[_] : Applicative, A](fa: Option[A])(f: (A) => G[Boolean]): G[Option[A]] = {
      fa match {
        case _: None.type => Option.empty[A].pure[G]
        case Some(a) => f(a).map(b => if (b) Some(a) else None)
      }
    }

    override def filter[A](fa: Option[A])(f: (A) => Boolean): Option[A] = fa.filter(f)

    override def traverse[G[_] : Applicative, A, B](fa: Option[A])(f: (A) => G[B]): G[Option[B]] = {
      cats.instances.option.catsStdInstancesForOption.traverse(fa)(f)
    }

    override def foldLeft[A, B](fa: Option[A], b: B)(f: (B, A) => B): B = {
      cats.instances.option.catsStdInstancesForOption.foldLeft(fa, b)(f)
    }

    override def foldRight[A, B](fa: Option[A], lb: Eval[B])(f: (A, Eval[B]) => Eval[B]): Eval[B] = {
      cats.instances.option.catsStdInstancesForOption.foldRight(fa, lb)(f)
    }
  }

  implicit val listTraverseEmpty: TraverseEmpty[List] = new TraverseEmpty[List] {
    override val functorEmpty: FunctorEmpty[List] = new FunctorEmpty[List] {
      override def mapFilter[A, B](fa: List[A])(f: (A) => Option[B])(implicit ev: Monad[List]): List[B] = {
        fa.collect(Function.unlift(f))
      }

      override def empty[A]: List[A] = Nil

      override val functor: Functor[List] = cats.instances.list.catsStdInstancesForList
    }

    override def traverseFilter[G[_] : Applicative, A, B](fa: List[A])(f: (A) => G[Option[B]]): G[List[B]] = {
      fa.foldLeft(List.empty[B].pure[G])(
        (xs, x) => Applicative[G].product(f(x), xs).map { case (i, o) => i.fold(o)(_ :: o) }
      )
    }

    override def filterA[G[_], A](fa: List[A])(f: (A) => G[Boolean])(implicit G: Applicative[G]): G[List[A]] = {
      fa.foldLeft(G.pure(List.empty[A]))(
        (xs, x) => G.product(f(x), xs).map(z => if (z._1) x :: z._2 else z._2)
      )
    }

    override def filter[A](fa: List[A])(f: (A) => Boolean): List[A] = fa.filter(f)

    override def traverse[G[_] : Applicative, A, B](fa: List[A])(f: (A) => G[B]): G[List[B]] = {
      cats.instances.list.catsStdInstancesForList.traverse(fa)(f)
    }

    override def foldLeft[A, B](fa: List[A], b: B)(f: (B, A) => B): B = {
      cats.instances.list.catsStdInstancesForList.foldLeft(fa, b)(f)
    }

    override def foldRight[A, B](fa: List[A], lb: Eval[B])(f: (A, Eval[B]) => Eval[B]): Eval[B] = {
      cats.instances.list.catsStdInstancesForList.foldRight(fa, lb)(f)
    }
  }

  implicit val vectorTraverseEmpty: TraverseEmpty[Vector] = new TraverseEmpty[Vector] {
    override val functorEmpty: FunctorEmpty[Vector] = new FunctorEmpty[Vector] {
      override def mapFilter[A, B](fa: Vector[A])(f: (A) => Option[B])(implicit ev: Monad[Vector]): Vector[B] = {
        fa.collect(Function.unlift(f))
      }

      override def empty[A]: Vector[A] = Vector.empty

      override val functor: Functor[Vector] = cats.instances.vector.catsStdInstancesForVector
    }

    override def traverseFilter[G[_] : Applicative, A, B](fa: Vector[A])(f: (A) => G[Option[B]]): G[Vector[B]] = {
      fa.foldLeft(Vector.empty[B].pure[G])(
        (xs, x) => Applicative[G].product(f(x), xs).map { case (i, o) => i.fold(o)(_ +: o) }
      )
    }

    override def filterA[G[_], A](fa: Vector[A])(f: (A) => G[Boolean])(implicit G: Applicative[G]): G[Vector[A]] = {
      fa.foldLeft(G.pure(Vector.empty[A]))(
        (xs, x) => G.product(f(x), xs).map(z => if (z._1) x +: z._2 else z._2)
      )
    }

    override def filter[A](fa: Vector[A])(f: (A) => Boolean): Vector[A] = fa.filter(f)

    override def traverse[G[_] : Applicative, A, B](fa: Vector[A])(f: (A) => G[B]): G[Vector[B]] = {
      cats.instances.vector.catsStdInstancesForVector.traverse(fa)(f)
    }

    override def foldLeft[A, B](fa: Vector[A], b: B)(f: (B, A) => B): B = {
      cats.instances.vector.catsStdInstancesForVector.foldLeft(fa, b)(f)
    }

    override def foldRight[A, B](fa: Vector[A], lb: Eval[B])(f: (A, Eval[B]) => Eval[B]): Eval[B] = {
      cats.instances.vector.catsStdInstancesForVector.foldRight(fa, lb)(f)
    }
  }

  implicit val streamTraverseEmpty: TraverseEmpty[Stream] = new TraverseEmpty[Stream] {
    override val functorEmpty: FunctorEmpty[Stream] = new FunctorEmpty[Stream] {
      override def mapFilter[A, B](fa: Stream[A])(f: (A) => Option[B])(implicit ev: Monad[Stream]): Stream[B] = {
        fa.collect(Function.unlift(f))
      }

      override def empty[A]: Stream[A] = Stream.empty

      override val functor: Functor[Stream] = cats.instances.stream.catsStdInstancesForStream
    }

    override def traverseFilter[G[_] : Applicative, A, B](fa: Stream[A])(f: (A) => G[Option[B]]): G[Stream[B]] = {
      fa.foldLeft(Stream.empty[B].pure[G])(
        (xs, x) => Applicative[G].product(f(x), xs).map { case (i, o) => i.fold(o)(_ +: o) }
      )
    }

    override def filterA[G[_], A](fa: Stream[A])(f: (A) => G[Boolean])(implicit G: Applicative[G]): G[Stream[A]] = {
      fa.foldLeft(G.pure(Stream.empty[A]))(
        (xs, x) => G.product(f(x), xs).map(z => if (z._1) x +: z._2 else z._2)
      )
    }

    override def filter[A](fa: Stream[A])(f: (A) => Boolean): Stream[A] = fa.filter(f)

    override def traverse[G[_] : Applicative, A, B](fa: Stream[A])(f: (A) => G[B]): G[Stream[B]] = {
      cats.instances.stream.catsStdInstancesForStream.traverse(fa)(f)
    }

    override def foldLeft[A, B](fa: Stream[A], b: B)(f: (B, A) => B): B = {
      cats.instances.stream.catsStdInstancesForStream.foldLeft(fa, b)(f)
    }

    override def foldRight[A, B](fa: Stream[A], lb: Eval[B])(f: (A, Eval[B]) => Eval[B]): Eval[B] = {
      cats.instances.stream.catsStdInstancesForStream.foldRight(fa, lb)(f)
    }
  }

}
