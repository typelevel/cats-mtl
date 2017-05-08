package cats
package mtl
package evidence

object Caps {

  trait HighPri extends LowPri1 {
    implicit def readerCanAsk[E, M[_]]: CanDo[CurryT[ReaderTC[E]#l, M]#l, EffAsk[E]] =
      new CanDo[CurryT[ReaderTC[E]#l, M]#l, EffAsk[E]]

    implicit def readerCanLocal[E, M[_]]: CanDo[CurryT[ReaderTC[E]#l, M]#l, EffLocal[E]] =
      new CanDo[CurryT[ReaderTC[E]#l, M]#l, EffLocal[E]]
  }

  trait LowPri1 {
//    implicit def readerCannot[E, M[_], Eff]: CanDo[CurryT[ReaderTC[E]#l, M]#l, Eff] =
//      new CanDo[CurryT[ReaderTC[E]#l, M]#l, Eff] {
//        override type Out = Bool.False
//      }
  }

}

object Capabilities extends Caps.HighPri
