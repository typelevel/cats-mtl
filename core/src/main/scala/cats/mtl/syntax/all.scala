package cats.mtl.syntax

object all extends AskSyntax
  with ListenSyntax with LocalSyntax
  with RaiseSyntax with StateSyntax
  with TellSyntax
  with EmptySyntax

