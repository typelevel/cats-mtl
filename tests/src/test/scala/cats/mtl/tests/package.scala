package cats
package mtl

import cats.mtl.laws.IsEq
import org.scalacheck.Prop
import Prop._

package object tests {
  implicit def catsLawsIsEqToProp[A: Eq](isEq: IsEq[A]): Prop =
    isEq.lhs ?= isEq.rhs
}
