package cats
package mtl
package evidence

sealed trait Bool
object Bool {
  final class True extends Bool
  final class False extends Bool
}

sealed trait BoolList
object BoolList {
  final class Nil extends BoolList
  final class Cons[B <: Bool, T <: BoolList] extends BoolList
}

