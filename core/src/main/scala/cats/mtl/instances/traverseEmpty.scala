package cats
package mtl
package instances

import cats.data.{Const, Nested}

object traverseEmpty extends TraverseEmptyInstances {

}

trait TraverseEmptyInstances {

  import cats.syntax.all._

  implicit val optionTraverseEmpty: TraverseEmpty[Option] = new TraverseEmpty[Option] {
    override val traverse: Traverse[Option] = cats.instances.option.catsStdInstancesForOption

    override val functorEmpty: FunctorEmpty[Option] = new FunctorEmpty[Option] {
      override val functor: Functor[Option] = cats.instances.option.catsStdInstancesForOption

      override def mapFilter[A, B](fa: Option[A])(f: (A) => Option[B]): Option[B] = fa.flatMap(f)

      override def filter[A](fa: Option[A])(f: (A) => Boolean): Option[A] = fa.filter(f)

      override def collect[A, B](fa: Option[A])(f: PartialFunction[A, B]): Option[B] = fa.collect(f)

      override def flattenOption[A](fa: Option[Option[A]]): Option[A] = fa.flatten
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

  }

  implicit val listTraverseEmpty: TraverseEmpty[List] = new TraverseEmpty[List] {
    override val traverse: Traverse[List] = cats.instances.list.catsStdInstancesForList

    override val functorEmpty: FunctorEmpty[List] = new FunctorEmpty[List] {
      override def mapFilter[A, B](fa: List[A])(f: (A) => Option[B]): List[B] = fa.collect(Function.unlift(f))

      override def filter[A](fa: List[A])(f: (A) => Boolean): List[A] = fa.filter(f)

      override val functor: Functor[List] = cats.instances.list.catsStdInstancesForList

      override def collect[A, B](fa: List[A])(f: PartialFunction[A, B]): List[B] = fa.collect(f)

      override def flattenOption[A](fa: List[Option[A]]): List[A] = fa.flatten
    }

    override def traverseFilter[G[_] : Applicative, A, B](fa: List[A])(f: (A) => G[Option[B]]): G[List[B]] = {
      fa.foldRight(List.empty[B].pure[G].pure[Eval])(
        (x, xse) =>
          xse.map(xs =>
            Applicative[G].product(f(x), xs).map { case (i, o) => i.fold(o)(_ :: o) }
          )
      ).value
    }

    override def filterA[G[_], A](fa: List[A])(f: (A) => G[Boolean])(implicit G: Applicative[G]): G[List[A]] = {
      fa.foldRight(List.empty[A].pure[G].pure[Eval])(
        (x, xse) => xse.map(xs =>
          G.product(f(x), xs).map(z => if (z._1) x :: z._2 else z._2)
        )
      ).value
    }
  }

  implicit val vectorTraverseEmpty: TraverseEmpty[Vector] = new TraverseEmpty[Vector] {
    override val traverse: Traverse[Vector] = cats.instances.vector.catsStdInstancesForVector

    override val functorEmpty: FunctorEmpty[Vector] = new FunctorEmpty[Vector] {
      override def mapFilter[A, B](fa: Vector[A])(f: (A) => Option[B]): Vector[B] = {
        fa.collect(Function.unlift(f))
      }

      override def filter[A](fa: Vector[A])(f: (A) => Boolean): Vector[A] = fa.filter(f)

      override val functor: Functor[Vector] = cats.instances.vector.catsStdInstancesForVector

      override def collect[A, B](fa: Vector[A])(f: PartialFunction[A, B]): Vector[B] = fa.collect(f)

      override def flattenOption[A](fa: Vector[Option[A]]): Vector[A] = fa.flatten
    }

    override def traverseFilter[G[_] : Applicative, A, B](fa: Vector[A])(f: (A) => G[Option[B]]): G[Vector[B]] = {
      fa.foldRight(Vector.empty[B].pure[G].pure[Eval])(
        (x, xse) => xse.map(xs =>
          Applicative[G].product(f(x), xs).map { case (i, o) => i.fold(o)(_ +: o) }
        )
      ).value
    }

    override def filterA[G[_], A](fa: Vector[A])(f: (A) => G[Boolean])(implicit G: Applicative[G]): G[Vector[A]] = {
      fa.foldRight(Vector.empty[A].pure[G].pure[Eval])(
        (x, xse) => xse.map(xs =>
          G.product(f(x), xs).map(z => if (z._1) x +: z._2 else z._2)
        )
      ).value
    }
  }

  implicit val streamTraverseEmpty: TraverseEmpty[Stream] = new TraverseEmpty[Stream] {
    override val traverse: Traverse[Stream] = cats.instances.stream.catsStdInstancesForStream

    override val functorEmpty: FunctorEmpty[Stream] = new FunctorEmpty[Stream] {
      override def mapFilter[A, B](fa: Stream[A])(f: (A) => Option[B]): Stream[B] = {
        fa.collect(Function.unlift(f))
      }

      override def filter[A](fa: Stream[A])(f: (A) => Boolean): Stream[A] = fa.filter(f)

      override val functor: Functor[Stream] = cats.instances.stream.catsStdInstancesForStream

      override def collect[A, B](fa: Stream[A])(f: PartialFunction[A, B]): Stream[B] = fa.collect(f)

      override def flattenOption[A](fa: Stream[Option[A]]): Stream[A] = fa.flatten
    }

    override def traverseFilter[G[_] : Applicative, A, B](fa: Stream[A])(f: (A) => G[Option[B]]): G[Stream[B]] = {
      fa.foldRight(Stream.empty[B].pure[G].pure[Eval])(
        (x, xse) => xse.map(xs =>
          Applicative[G].product(f(x), xs).map { case (i, o) => i.fold(o)(_ +: o) }
        )
      ).value
    }

    override def filterA[G[_], A](fa: Stream[A])(f: (A) => G[Boolean])(implicit G: Applicative[G]): G[Stream[A]] = {
      fa.foldRight(Stream.empty[A].pure[G].pure[Eval])(
        (x, xse) => xse.map(xs =>
          G.product(f(x), xs).map(z => if (z._1) x +: z._2 else z._2)
        )
      ).value
    }

  }

  type ConstC[C] = {type l[A] = Const[C, A]}
  val constTraverseEmptyAny: TraverseEmpty[ConstC[Any]#l] = new TraverseEmpty[ConstC[Any]#l] {
    override val functorEmpty: FunctorEmpty[ConstC[Any]#l] = new FunctorEmpty[ConstC[Any]#l] {
      override val functor: Functor[ConstC[Any]#l] = Const.catsDataTraverseFilterForConst

      override def mapFilter[A, B](fa: Const[Any, A])(f: (A) => Option[B]): Const[Any, B] = ???

      override def collect[A, B](fa: Const[Any, A])(f: PartialFunction[A, B]): Const[Any, B] = ???

      override def flattenOption[A](fa: Const[Any, Option[A]]): Const[Any, A] = ???

      override def filter[A](fa: Const[Any, A])(f: (A) => Boolean): Const[Any, A] = ???
    }

    override def traverseFilter[G[_] : Applicative, A, B](fa: Const[Any, A])(f: (A) => G[Option[B]]): G[Const[Any, B]] = {
      fa.retag[B].pure[G]
    }

    override def filterA[G[_] : Applicative, A](fa: Const[Any, A])(f: (A) => G[Boolean]): G[Const[Any, A]] = {
      fa.pure[G]
    }

    override val traverse: Traverse[ConstC[Any]#l] = Const.catsDataTraverseFilterForConst[Any]
  }

  implicit def constTraverseEmpty[C]: TraverseEmpty[ConstC[C]#l] = {
    constTraverseEmptyAny.asInstanceOf[TraverseEmpty[ConstC[C]#l]]
  }

  implicit def catsDataTraverseEmptyForNested[F[_] : Traverse, G[_] : TraverseEmpty]: TraverseEmpty[NestedC[F, G]#l] = {
    new NestedTraverseEmpty[F, G] {
      val F: Traverse[F] = Traverse[F]
      val G: TraverseEmpty[G] = TraverseEmpty[G]
    }
  }

  type NestedC[F[_], G[_]] = {type l[A] = Nested[F, G, A]}

  private[cats] trait NestedTraverseEmpty[F[_], G[_]] extends TraverseEmpty[NestedC[F, G]#l] {
    implicit def F: Traverse[F]

    implicit def G: TraverseEmpty[G]

    override val traverse: Traverse[NestedC[F, G]#l] = Nested.catsDataTraverseForNested(F, G.traverse)

    override val functorEmpty: FunctorEmpty[NestedC[F, G]#l] = new FunctorEmpty[NestedC[F, G]#l] {
      override val functor: Functor[NestedC[F, G]#l] = Nested.catsDataFunctorForNested[F, G](F, G.traverse)

      override def mapFilter[A, B](fa: Nested[F, G, A])(f: (A) => Option[B]): Nested[F, G, B] = {
        Nested[F, G, B](F.map(fa.value)(G.functorEmpty.mapFilter(_)(f)))
      }

      override def collect[A, B](fa: Nested[F, G, A])(f: PartialFunction[A, B]): Nested[F, G, B] = {
        Nested[F, G, B](F.map(fa.value)(G.functorEmpty.collect(_)(f)))
      }

      override def flattenOption[A](fa: Nested[F, G, Option[A]]): Nested[F, G, A] = {
        Nested[F, G, A](F.map(fa.value)(G.functorEmpty.flattenOption(_)))
      }

      override def filter[A](fa: Nested[F, G, A])(f: (A) => Boolean): Nested[F, G, A] = {
        Nested[F, G, A](F.map(fa.value)(G.functorEmpty.filter(_)(f)))
      }
    }

    override def filterA[H[_] : Applicative, A](fa: Nested[F, G, A])(f: A => H[Boolean]): H[Nested[F, G, A]] = {
      F.traverse(fa.value)(G.filterA[H, A](_)(f)).map(Nested[F, G, A])
    }

    override def traverseFilter[H[_] : Applicative, A, B](fga: Nested[F, G, A])(f: A => H[Option[B]]): H[Nested[F, G, B]] = {
      F.traverse[H, G[A], G[B]](fga.value)(ga => G.traverseFilter(ga)(f)).map(Nested[F, G, B])
    }
  }

}
