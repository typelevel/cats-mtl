package cats.mtl.special

private[special] trait Newtype2 { self =>
  private[special] type Base
  private[special] trait Tag extends Any
  type Type[A, B] <: Base with Tag
}

