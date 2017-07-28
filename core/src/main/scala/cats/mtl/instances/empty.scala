package cats
package mtl
package instances

import cats.data.{Const, EitherT, Nested, OptionT}

object empty extends EmptyInstances

import cats.syntax.functor._
import cats.syntax.traverse._
import cats.syntax.applicative._
import cats.syntax.either._
import cats.instances.option._
import cats.instances.either._

trait EmptyInstances extends EmptyInstances1 {

  implicit def optionTFunctorEmpty[F[_] : Functor]: FunctorEmpty[OptionTC[F]#l] = {
    new FunctorEmpty[OptionTC[F]#l] {
      override val functor: Functor[OptionTC[F]#l] = OptionT.catsDataFunctorForOptionT[F]

      override def mapFilter[A, B](fa: OptionT[F, A])(f: (A) => Option[B]): OptionT[F, B] = fa.subflatMap(f)

      override def collect[A, B](fa: OptionT[F, A])(f: PartialFunction[A, B]): OptionT[F, B] = fa.subflatMap(f.lift)

      override def flattenOption[A](fa: OptionT[F, Option[A]]): OptionT[F, A] = fa.subflatMap(identity)

      override def filter[A](fa: OptionT[F, A])(f: (A) => Boolean): OptionT[F, A] = fa.filter(f)
    }
  }

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
      override val functor: Functor[ConstC[Any]#l] = Const.catsDataTraverseForConst[Any]

      override def mapFilter[A, B](fa: Const[Any, A])(f: (A) => Option[B]): Const[Any, B] = fa.retag

      override def collect[A, B](fa: Const[Any, A])(f: PartialFunction[A, B]): Const[Any, B] = fa.retag

      override def flattenOption[A](fa: Const[Any, Option[A]]): Const[Any, A] = fa.retag

      override def filter[A](fa: Const[Any, A])(f: (A) => Boolean): Const[Any, A] = fa.retag
    }

    override def traverseFilter[G[_] : Applicative, A, B](fa: Const[Any, A])(f: (A) => G[Option[B]]): G[Const[Any, B]] = {
      fa.retag[B].pure[G]
    }

    override def filterA[G[_] : Applicative, A](fa: Const[Any, A])(f: (A) => G[Boolean]): G[Const[Any, A]] = {
      fa.pure[G]
    }

    override val traverse: Traverse[ConstC[Any]#l] = Const.catsDataTraverseForConst[Any]
  }

  implicit def constTraverseEmpty[C]: TraverseEmpty[ConstC[C]#l] = {
    constTraverseEmptyAny.asInstanceOf[TraverseEmpty[ConstC[C]#l]]
  }

  implicit def catsDataTraverseEmptyForNested[F[_], G[_]](implicit F0: Traverse[F], G0: TraverseEmpty[G]): TraverseEmpty[NestedC[F, G]#l] = {
    new {
      val F: Traverse[F] = F0
      val G: TraverseEmpty[G] = G0
    } with NestedTraverseEmpty[F, G]
  }

  implicit def mapTraverseEmpty[K]: TraverseEmpty[MapC[K]#l] = {
    new TraverseEmpty[MapC[K]#l] {
      override def traverseFilter[G[_], A, B](fa: Map[K, A])(f: A => G[Option[B]])(implicit G: Applicative[G]): G[Map[K, B]] = {
        val gba: Eval[G[Map[K, B]]] = Always(G.pure(Map.empty))
        val gbb = Foldable.iterateRight(fa.iterator, gba) { (kv, lbuf) =>
          G.map2Eval(f(kv._2), lbuf)({ (ob, buf) => ob.fold(buf)(b => buf + (kv._1 -> b)) })
        }.value
        G.map(gbb)(_.toMap)
      }

      override val traverse: Traverse[MapC[K]#l] = cats.instances.map.catsStdInstancesForMap[K]
      override val functorEmpty: FunctorEmpty[MapC[K]#l] = new FunctorEmpty[MapC[K]#l] {
        override val functor: Functor[MapC[K]#l] = traverse

        override def mapFilter[A, B](fa: Map[K, A])(f: (A) => Option[B]): Map[K, B] = {
          fa.collect(scala.Function.unlift(t => f(t._2).map(t._1 -> _)))
        }

        override def collect[A, B](fa: Map[K, A])(f: PartialFunction[A, B]): Map[K, B] = {
          fa.collect(scala.Function.unlift(t => f.lift(t._2).map(t._1 -> _)))
        }

        override def flattenOption[A](fa: Map[K, Option[A]]): Map[K, A] = {
          fa.collect(scala.Function.unlift(t => t._2.map(t._1 -> _)))
        }

        override def filter[A](fa: Map[K, A])(f: (A) => Boolean): Map[K, A] = {
          fa.filter { case (_, v) => f(v) }
        }
      }

      override def filterA[G[_], A](fa: Map[K, A])(f: (A) => G[Boolean])(implicit G: Applicative[G]): G[Map[K, A]] = {
        traverseFilter(fa)(a => G.map(f(a))(if (_) Some(a) else None))
      }
    }
  }

}

private[cats] abstract class NestedTraverseEmpty[F[_], G[_]] extends TraverseEmpty[NestedC[F, G]#l] {
  implicit val F: Traverse[F]

  implicit val G: TraverseEmpty[G]

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
      Nested[F, G, A](F.map(fa.value)(G.functorEmpty.flattenOption))
    }

    override def filter[A](fa: Nested[F, G, A])(f: (A) => Boolean): Nested[F, G, A] = {
      Nested[F, G, A](F.map(fa.value)(G.functorEmpty.filter(_)(f)))
    }
  }

  override def filterA[H[_] : Applicative, A](fa: Nested[F, G, A])(f: A => H[Boolean]): H[Nested[F, G, A]] = {
    F.traverse(fa.value)(G.filterA[H, A](_)(f)).map(Nested[F, G, A])
  }

  override def traverseFilter[H[_] : Applicative, A, B](fga: Nested[F, G, A]
                                                       )(f: A => H[Option[B]]): H[Nested[F, G, B]] = {
    F.traverse[H, G[A], G[B]](fga.value)(ga => G.traverseFilter(ga)(f)).map(Nested[F, G, B])
  }
}


trait EmptyInstances1 {
  implicit def functorEmptyLiftEitherT[M[_], E](implicit under: FunctorEmpty[M]): FunctorEmpty[EitherTC[M, E]#l] = {
    new FunctorEmpty[EitherTC[M, E]#l] {
      override lazy val functor: Functor[EitherTC[M, E]#l] = EitherT.catsDataFunctorForEitherT(under.functor)
      implicit lazy val func: Functor[M] = under.functor

      override def mapFilter[A, B](fa: EitherT[M, E, A])(f: (A) => Option[B]): EitherT[M, E, B] = {
        EitherT[M, E, B](under.mapFilter(fa.value)(_.traverse(f)))
      }

      override def collect[A, B](fa: EitherT[M, E, A])(f: PartialFunction[A, B]): EitherT[M, E, B] = {
        EitherT[M, E, B](under.mapFilter(fa.value)(_.traverse(f.lift)))
      }

      override def flattenOption[A](fa: EitherT[M, E, Option[A]]): EitherT[M, E, A] = {
        EitherT[M, E, A](under.flattenOption[E Either A](fa.value.map(_.sequence)))
      }

      override def filter[A](fa: EitherT[M, E, A])(f: (A) => Boolean): EitherT[M, E, A] = {
        EitherT[M, E, A](under.filter(fa.value)(_.forall(f)))
      }
    }
  }

  implicit def traverseEmptyLiftEitherT[M[_], E](implicit under: TraverseEmpty[M]): TraverseEmpty[EitherTC[M, E]#l] = {
    new TraverseEmpty[EitherTC[M, E]#l] {
      override val functorEmpty: FunctorEmpty[EitherTC[M, E]#l] = functorEmptyLiftEitherT(under.functorEmpty)
      override val traverse: Traverse[EitherTC[M, E]#l] = EitherT.catsDataTraverseForEitherT[M, E](under.traverse)

      override def traverseFilter[G[_], A, B](fa: EitherT[M, E, A])
                                             (f: (A) => G[Option[B]])
                                             (implicit G: Applicative[G]): G[EitherT[M, E, B]] = {
        G.map(
          under.traverseFilter[G, E Either A, E Either B](fa.value)(e =>
            e.fold[G[Option[E Either B]]](_ => Option(e.asInstanceOf[E Either B]).pure[G], f(_).map(_.map(Either.right(_))))
          )
        )(EitherT(_))
      }

      override def filterA[G[_], A](fa: EitherT[M, E, A])(f: (A) => G[Boolean])(implicit G: Applicative[G]): G[EitherT[M, E, A]] = {
        G.map(under.filterA(fa.value)(_.fold(_ => G.pure(true), f)))(EitherT[M, E, A])
      }
    }
  }
}
