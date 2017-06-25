package cats
package mtl
package laws

import org.scalacheck.Prop
import Prop._

package object discipline {
  implicit def catsLawsIsEqToProp[A: Eq](isEq: IsEq[A]): Prop = {
    isEq.lhs ?= isEq.rhs
  }
}
