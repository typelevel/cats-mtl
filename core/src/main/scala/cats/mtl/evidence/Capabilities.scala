package cats
package mtl
package evidence

import cats.mtl.CurryT

trait Capabilities {

  trait HighPri extends LowPri1 {
    implicit def readerCanAsk[E, M[_]]: CanDo[CurryT[ReaderTC[E]#l, M]#l, EffAsk[E]] =
      new CanDo[CurryT[ReaderTC[E]#l, M]#l, EffAsk[E]] {
        override type Out = Bool.True
      }

    implicit def readerCanLocal[E, M[_]]: CanDo[CurryT[ReaderTC[E]#l, M]#l, EffLocal[E]] =
      new CanDo[CurryT[ReaderTC[E]#l, M]#l, EffLocal[E]] {
        override type Out = Bool.True
      }
  }

  trait LowPri1 {
    implicit def readerCannot[E, M[_], Eff]: CanDo[CurryT[ReaderTC[E]#l, M]#l, Eff] =
      new CanDo[CurryT[ReaderTC[E]#l, M]#l, Eff] {
        override type Out = Bool.False
      }
  }

}
