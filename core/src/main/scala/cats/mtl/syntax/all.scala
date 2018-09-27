package cats.mtl.syntax

object all extends AllSyntax

trait AllSyntax extends AskSyntax
  with ListenSyntax
  with LocalSyntax
  with RaiseSyntax
  with StateSyntax
  with TellSyntax
  with HandleSyntax
  with ChronicleSyntax
