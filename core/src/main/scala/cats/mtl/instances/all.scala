package cats
package mtl
package instances

object all extends AskInstances
  with EitherTInstances
  with HandleInstances
  with ListenInstances
  with OptionTInstances
  with RaiseInstances
  with ReaderTInstances
  with LocalInstances
  with StateInstances
  with StateTInstances
  with TellInstances
  with WriterTInstances

