package cats
package mtl
package laws

trait MonadLayerControlLaws[M[_], Inner[_]] extends MonadLayerFunctorLaws[M, Inner] {

}

object MonadLayerControlLaws {
  def apply[M[_], Inner[_]](implicit monadLayerControlLaws: MonadLayerControlLaws[M, Inner]
                           ): MonadLayerControlLaws[M, Inner] = monadLayerControlLaws
}
