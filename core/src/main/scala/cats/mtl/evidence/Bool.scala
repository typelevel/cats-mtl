package cats.mtl.evidence

sealed trait Bool
object Bool {
  sealed trait True extends Bool
  sealed trait False extends Bool
}

sealed trait BoolList
object BoolList {
  sealed trait Nil extends BoolList
  sealed trait Cons[B <: Bool, T <: BoolList] extends BoolList
}
