package cats.mtl.syntax

object all

trait AllSyntax extends AskSyntax
  with ListenSyntax
  with LocalSyntax
  with RaiseSyntax
  with StateSyntax
  with TellSyntax
  with EmptySyntax
  with HandleSyntax

