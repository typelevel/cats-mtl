package cats
package mtl
package instances

object all extends AllInstances

trait AllInstances extends EitherTInstances
  with CensorInstances with OptionTInstances
  with RaiseInstances with ReaderTInstances
  with LocalInstances with StateInstances
  with StateTInstances with WriterTInstances
  with ReaderWriterStateTInstances
  with HandleInstances
  with ChronicleInstances with IorTInstances
