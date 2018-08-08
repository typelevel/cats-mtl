package cats
package mtl
package instances

object all extends AllInstances

trait AllInstances extends EitherTInstances
  with ListenInstances with OptionTInstances
  with RaiseInstances with ReaderTInstances
  with LocalInstances with StateInstances
  with StateTInstances with WriterTInstances
  with EmptyInstances with ReaderWriterStateTInstances
  with HandleInstances

