package cats
package mtl
package laws

import org.scalacheck.Prop
import cats.kernel.laws._

package object discipline {
  implicit def catsLawsIsEqToProp[A: Eq](isEq: IsEq[A]): Prop = {
    isEq.lhs ?== isEq.rhs
  }
}
