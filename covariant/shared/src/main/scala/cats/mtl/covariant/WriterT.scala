package cats.mtl.covariant

import cats._
import cats.syntax.semigroup._

final class WriterT[F[+_], +L, +V](val run: F[(L, V)]) {

  /**
   * Example:
   * {{{
   * scala> import cats.mtl.covariant.WriterT
   * scala> import cats.implicits._
   *
   * scala> val writer = WriterT.liftF[Option, List[String], Int](Some(123))
   * scala> writer.tell(List("a","b","c")).tell(List("d","e","f"))
   * res0: WriterT[Option, List[String], Int] = WriterT(Some((List(a, b, c, d, e, f),123)))
   * }}}
   */
  def tell[LL >: L](l: LL)(implicit functorF: Functor[F], semigroupL: Semigroup[LL]): WriterT[F, LL, V] =
    mapWritten(semigroupL.combine(_, l))

  /**
   * Example:
   * {{{
   * scala> import cats.mtl.covariant.WriterT
   * scala> import cats.implicits._
   *
   * scala> val writer: WriterT[Option, List[String], Int] = WriterT.liftF(Some(123))
   * scala> writer.tell(List("a","b","c")).written.getOrElse(Nil)
   * res0: List[String] = List(a, b, c)
   * }}}
   */
  def written(implicit functorF: Functor[F]): F[L] =
    functorF.map(run)(_._1)

  /**
   * Example:
   * {{{
   * scala> import cats.mtl.covariant.WriterT
   * scala> import cats.implicits._
   *
   * scala> val writer: WriterT[Option, List[String], Int] = WriterT.liftF(Some(123))
   * scala> val wt: WriterT[Option, List[String], Int] = writer.tell(List("error"))
   * res0: WriterT[Option, List[String], Int] = WriterT(Some((List(error),123)))
   *
   * scala> wt.value
   * res1: Option[Int] = Some(123)
   * }}}
   */
  def value(implicit functorF: Functor[F]): F[V] =
    functorF.map(run)(_._2)

  /**
   * Example:
   * {{{
   * scala> import cats.mtl.covariant.WriterT
   * scala> import cats.implicits._
   *
   * scala> val writer: WriterT[Option, String, Int] = WriterT.liftF(Some(123))
   * scala> val wt: WriterT[Option, String, Int] = writer.tell("error").tell(" log")
   * res0: WriterT[Option, String, Int] = WriterT(Some((error log,123)))
   *
   * scala> wt.listen
   * res1: WriterT[Option, String, (Int,String)] = WriterT(Some((error log,(123,error log))))
   * }}}
   */
  def listen(implicit F: Functor[F]): WriterT[F, L, (V, L)] =
    WriterT(F.map(run) {
      case (l, v) => (l, (v, l))
    })

  /**
   * Example:
   * {{{
   * scala> import cats.mtl.covariant.WriterT
   * scala> import cats.implicits._
   *
   * scala> val writer: WriterT[Option, String, Int] = WriterT.liftF(Some(123))
   * scala> val wt: WriterT[Option, String, Int] = writer.tell("error")
   * res0: WriterT[Option, String, Int] = WriterT(Some((error,123)))
   *
   * scala> val func = WriterT.liftF[Option, String, Int => List[Int]](Some(i => List(i)))
   * scala> val func2 = func.tell("log")
   *
   * scala> wt.ap(func2)
   * res1: WriterT[Option, String, List[Int]] = WriterT(Some((logerror,List(123))))
   * }}}
   */
  def ap[LL >: L, Z](f: WriterT[F, LL, V => Z])(implicit F: Apply[F], L: Semigroup[LL]): WriterT[F, LL, Z] =
    WriterT(F.map2(f.run, run) {
      case ((l1, fvz), (l2, v)) => (L.combine(l1, l2), fvz(v))
    })

  /**
   * Example:
   * {{{
   * scala> import cats.mtl.covariant.WriterT
   * scala> import cats.implicits._
   *
   * scala> val wr1: WriterT[Option, String, Int] = WriterT.liftF(None)
   * scala> val wr2 = wr1.tell("error")
   * res0: WriterT[Option, String, Int] = WriterT(None)
   *
   * scala> wr2.map(_ * 2)
   * res1: WriterT[Option, String, Int] = WriterT(None)
   *
   * scala> val wr3: WriterT[Option, String, Int] = WriterT.liftF(Some(456))
   * scala> val wr4 = wr3.tell("error")
   * scala> wr4.map(_ * 2)
   * res2: WriterT[Option, String, Int] = WriterT(Some((error,912)))
   * }}}
   */
  def map[Z](fn: V => Z)(implicit functorF: Functor[F]): WriterT[F, L, Z] =
    WriterT {
      functorF.map(run) { z =>
        (z._1, fn(z._2))
      }
    }

  /**
   * Modify the context `F` using transformation `f`.
   *
   * Example:
   * {{{
   * scala> import cats.mtl.covariant.WriterT
   * scala> import cats.arrow.FunctionK
   * scala> import cats.implicits._
   *
   * scala> val optionWriter = WriterT.liftF[Option, String, Int](Some(123)).tell("log")
   * res0: WriterT[Option, String, Int](Some((log,123)))
   *
   * scala> def toList[A](option: Option[A]): List[A] = option.toList
   * scala> val listWriter = optionWriter.mapK(FunctionK.lift(toList _))
   * res1: WriterT[List, String, Int](List((log,123)))
   * }}}
   */
  def mapK[G[+_]](f: F ~> G): WriterT[G, L, V] =
    WriterT[G, L, V](f(run))

  /**
   * Example:
   * {{{
   * scala> import cats.mtl.covariant.WriterT
   * scala> import cats.implicits._
   *
   * scala> val wr1 = WriterT.liftF[Option, String, Int](Some(123)).tell("error")
   * res0: WriterT[Option, String, Int] = WriterT(Some(error,123))
   * scala> val func = (i:Int) => WriterT.liftF[Option, String, Int](Some(i * 2)).tell(i.show)
   *
   * scala> wr1.flatMap(func)
   * res1: WriterT[Option, String, Int] = WriterT(Some((error123,246)))
   * }}}
   */
  def flatMap[LL >: L, U](f: V => WriterT[F, LL, U])(implicit flatMapF: FlatMap[F], semigroupL: Semigroup[LL]): WriterT[F, LL, U] =
    WriterT {
      flatMapF.flatMap(run) { lv =>
        flatMapF.map(f(lv._2).run) { lv2 =>
          (semigroupL.combine(lv._1, lv2._1), lv2._2)
        }
      }
    }

  /**
   * Example:
   * {{{
   * scala> import cats.mtl.covariant.WriterT
   * scala> import cats.implicits._
   *
   * scala> val wr1 = WriterT.liftF[Option, String, Int](Some(123)).tell("quack")
   * res0: WriterT[Option, String, Int] = WriterT(Some(quack,123))
   *
   * scala> wr1.mapBoth((s,i) => (s + " " + s, i * 2))
   * res1: WriterT[Option, String, Int] = WriterT(Some((quack quack,246)))
   * }}}
   */
  def mapBoth[M, U](f: (L, V) => (M, U))(implicit functorF: Functor[F]): WriterT[F, M, U] =
    WriterT { functorF.map(run)(f.tupled) }

  /**
   * Example:
   * {{{
   * scala> import cats.mtl.covariant.WriterT
   * scala> import cats.implicits._
   *
   * scala> val wr1 = WriterT.liftF[Option, String, Int](Some(123)).tell("456")
   * res0: WriterT[Option, String, Int] = WriterT(Some(456,123))
   *
   * scala> wr1.bimap(_.toInt, _.show)
   * res1: WriterT[Option, Int, String] = WriterT(Some((456,123)))
   * }}}
   */
  def bimap[M, U](f: L => M, g: V => U)(implicit functorF: Functor[F]): WriterT[F, M, U] =
    mapBoth((l, v) => (f(l), g(v)))

  /**
   * Example:
   * {{{
   * scala> import cats.mtl.covariant.WriterT
   * scala> import cats.implicits._
   *
   * scala> val writer = WriterT.liftF[Option, String, Int](Some(246)).tell("error")
   * res0: WriterT[Option, String, Int] = WriterT(Some((error,246)))
   *
   * scala> writer.mapWritten(i => List(i))
   * res1: WriterT[Option, List[String], Int] = WriterT(Some((List(error),246)))
   * }}}
   */
  def mapWritten[M](f: L => M)(implicit functorF: Functor[F]): WriterT[F, M, V] =
    mapBoth((l, v) => (f(l), v))

  /**
   * Example:
   * {{{
   * scala> import cats.mtl.covariant.WriterT
   * scala> import cats.implicits._
   *
   * scala> val writer = WriterT.liftF[Option, String, Int](Some(123)).tell("log")
   * scala> writer.swap
   * res0: WriterT[Option, Int, String] = WriterT(Some((123,log)))
   * }}}
   */
  def swap(implicit functorF: Functor[F]): WriterT[F, V, L] =
    mapBoth((l, v) => (v, l))

  /**
   * Example:
   * {{{
   * scala> import cats.mtl.covariant.WriterT
   * scala> import cats.implicits._
   *
   * scala> val writer = WriterT.liftF[Option, String, Int](Some(123)).tell("error")
   * scala> writer.reset
   * res0: WriterT[Option, String, Int] = WriterT(Some((,123)))
   * }}}
   */
  def reset[LL >: L](implicit monoidL: Monoid[LL], functorF: Functor[F]): WriterT[F, LL, V] =
    mapWritten(_ => monoidL.empty)

  /**
   * Example:
   * {{{
   * scala> import cats.mtl.covariant.WriterT
   * scala> import cats.implicits._
   *
   * scala> val writer = WriterT.liftF[Option, String, Int](Some(456)).tell("log...")
   * scala> writer.show
   * res0: String = Some((log...,456))
   * }}}
   */
  def show[LL >: L, VV >: V](implicit F: Show[F[(LL, VV)]]): String = F.show(run)

  /**
   * Example:
   * {{{
   * scala> import cats.mtl.covariant.WriterT
   * scala> import cats.implicits._
   *
   * scala> val writer = WriterT.liftF[Option, String, Int](Some(123)).tell("hi")
   * scala> writer.foldLeft(456)((acc,v) => acc + v)
   * res0: Int = 579
   * }}}
   */
  def foldLeft[C](c: C)(f: (C, V) => C)(implicit F: Foldable[F]): C =
    F.foldLeft(run, c)((z, lv) => f(z, lv._2))

  /**
   * Example:
   * {{{
   * scala> import cats.mtl.covariant.WriterT
   * scala> import cats.Eval
   * scala> import cats.implicits._
   *
   * scala> val writer = WriterT.liftF[Option, String, Int](Some(123)).tell("hi")
   * scala> writer
   *      |    .foldRight(Eval.now(456))((v,c) => c.map(_ + v))
   *      |    .value
   * res0: Int = 579
   * }}}
   */
  def foldRight[C](lc: Eval[C])(f: (V, Eval[C]) => Eval[C])(implicit F: Foldable[F]): Eval[C] =
    F.foldRight(run, lc)((lv, z) => f(lv._2, z))

  /**
   * Example:
   * {{{
   * scala> import cats.mtl.covariant.WriterT
   * scala> import cats.implicits._
   *
   * scala> val writer = WriterT.liftF[Option, String, Int](Some(123)).tell("hi")
   * scala> writer.traverse[List,Int](i => List(i))
   * res0: List[WriterT[Option, String, Int]] = List(WriterT(Some((hi,123))))
   * }}}
   */
  def traverse[G[_], LL >: L, V1](f: V => G[V1])(implicit F: Traverse[F], G: Applicative[G]): G[WriterT[F, LL, V1]] =
    G.map(
      F.traverse(run)(lv => G.tupleLeft(f(lv._2), lv._1))
    )(WriterT.apply)
}

object WriterT {
  def apply[F[+_], L, A](value: F[(L, A)]): WriterT[F, L, A] =
    new WriterT[F, L, A](value)

  def liftF[F[+_], L, V](fv: F[V])(implicit monoidL: Monoid[L], F: Applicative[F]): WriterT[F, L, V] =
    WriterT(F.map(fv)(v => (monoidL.empty, v)))

  /**
   * Same as [[liftF]], but expressed as a FunctionK for use with mapK
   * {{{
   * scala> import cats._, mtl.covariant._, implicits._
   * scala> val a: OptionT[Eval, Int] = 1.pure[OptionT[Eval, *]]
   * scala> val b: OptionT[WriterT[Eval, String, *], Int] = a.mapK(WriterT.liftK)
   * scala> b.value.run.value
   * res0: (String, Option[Int]) = ("",Some(1))
   * }}}
   */
  def liftK[F[+_], L](implicit monoidL: Monoid[L], F: Applicative[F]): F ~> WriterT[F, L, *] =
    new (F ~> WriterT[F, L, *]) {
      def apply[A](fa: F[A]): WriterT[F,L,A] = liftF(fa)
    }

  def putT[F[+_], L, V](vf: F[V])(l: L)(implicit functorF: Functor[F]): WriterT[F, L, V] =
    WriterT(functorF.map(vf)(v => (l, v)))

  def put[F[+_], L, V](v: V)(l: L)(implicit applicativeF: Applicative[F]): WriterT[F, L, V] =
    WriterT.putT[F, L, V](applicativeF.pure(v))(l)

  def tell[F[+_], L](l: L)(implicit applicativeF: Applicative[F]): WriterT[F, L, Unit] =
    WriterT.put[F, L, Unit](())(l)

  def value[F[+_], L, V](v: V)(implicit applicativeF: Applicative[F], monoidL: Monoid[L]): WriterT[F, L, V] =
    WriterT.put[F, L, V](v)(monoidL.empty)

  def valueT[F[+_], L, V](vf: F[V])(implicit functorF: Functor[F], monoidL: Monoid[L]): WriterT[F, L, V] =
    WriterT.putT[F, L, V](vf)(monoidL.empty)

}