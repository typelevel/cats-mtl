package cats
package mtl
package instances

import cats.data.WriterT
import cats.syntax.functor._

trait WriterTInstances extends WriterTInstances1

private[instances] trait WriterTInstances1 extends WriterTInstances2

private[instances] trait WriterTInstances2

object writert extends WriterTInstances

