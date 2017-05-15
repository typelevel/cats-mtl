package cats
package mtl

trait MonadTransControl[M[_]] extends MonadLayerControl[M] with MonadTrans[M] {
  def transControl[N[_], NInner[_], A](cps: (N ~> (NInner of State)#l) => NInner[A])
                                      (implicit mt: MonadTrans.AuxIO[N, NInner, Outer]): M[A]
}
