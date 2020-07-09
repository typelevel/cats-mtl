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

import cats.data.WriterT

import scala.annotation.implicitNotFound

/**
 * `Tell[F, L]` is the ability to "log" values `L` inside a context `F[_]`, as an effect.
 *
 * `Tell` has no external laws.
 *
 * `Tell` has one internal law:
 * {{{
 * def writerIsTellAndMap(a: A, l: L) = {
 *   (tell(l) as a) <-> writer(a, l)
 * }
 *
 * def tupleIsWriterFlipped(a: A, l: L) = {
 *   writer(a, l) <-> tuple((l, a))
 * }
 * }}}
 */
@implicitNotFound(
  "Could not find an implicit instance of Tell[${F}, ${L}]. If you wish\nto capture side-channel output of type ${L} at this location, you may want\nto construct a value of type WriterT for this call-site, rather than ${F}.\nAn example type:\n\n  WriterT[${F}, ${L}, *]\n\nOne use-case for this would be if ${L} represents an accumulation of values\nwhich are produced by this function *in addition to* its normal results.\nThis can be used to implement some forms of pure logging.\n\nIf you do not wish to capture a side-channel of type ${L} at this location,\nyou should add an implicit parameter of this type to your function. For\nexample:\n\n  (implicit ftell: Tell[${F}, ${L}}])\n")
trait Tell[F[_], -L] extends Serializable {
  def functor: Functor[F]

  def tell(l: L): F[Unit]

  def writer[A](a: A, l: L): F[A] = functor.as(tell(l), a)

  def tuple[A](ta: (L, A)): F[A] = writer(ta._2, ta._1)
}

private[mtl] trait TellMonadPartialOrder[F[_], G[_], L] extends Tell[G, L] {
  val lift: MonadPartialOrder[F, G]
  val F: Tell[F, L]

  override val functor = lift.monadG
  override def tell(l: L) = lift(F.tell(l))
}

private[mtl] trait LowPriorityTellInstances {

  implicit def tellForPartialOrder[F[_], G[_], L](
      implicit lift0: MonadPartialOrder[F, G],
      F0: Tell[F, L]): Tell[G, L] =
    new TellMonadPartialOrder[F, G, L] {
      val lift: MonadPartialOrder[F, G] = lift0
      val F: Tell[F, L] = F0
    }
}

private[mtl] trait TellInstances
    extends LowPriorityTellInstances
    with LowPriorityTellInstancesCompat {

  implicit def tellForWriterT[F[_]: Applicative, L: Monoid]: Tell[WriterT[F, L, *], L] =
    new Tell[WriterT[F, L, *], L] {
      val functor = Functor[WriterT[F, L, *]]
      def tell(l: L) = WriterT.tell[F, L](l)
    }
}

object Tell extends TellInstances {
  def apply[F[_], L](implicit tell: Tell[F, L]): Tell[F, L] = tell
  def tell[F[_], L](l: L)(implicit tell: Tell[F, L]): F[Unit] = tell.tell(l)

  def tellF[F[_]]: tellFPartiallyApplied[F] = new tellFPartiallyApplied[F]

  final private[mtl] class tellFPartiallyApplied[F[_]](val dummy: Boolean = false)
      extends AnyVal {
    @inline def apply[L](l: L)(implicit tell: Tell[F, L]): F[Unit] =
      tell.tell(l)
  }
}
