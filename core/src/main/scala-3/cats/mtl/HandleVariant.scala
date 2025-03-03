package cats
package mtl

trait HandleVariant { this: Handle.type =>
  import Handle.Submarine

  def allow[E]: AdHocSyntaxWired[E] =
    new AdHocSyntaxWired[E]

  final class AdHocSyntaxWired[E]:

    def apply[F[_], A](body: Handle[F, E] ?=> F[A])(implicit F: ApplicativeThrow[F]): Inner[F, A] =
      new Inner(body)

    final class Inner[F[_], A](body: Handle[F, E] ?=> F[A])(implicit F: ApplicativeThrow[F]):
      def rescue(h: E => F[A]): F[A] =
        val Marker = new AnyRef

        def inner[B](fb: F[B])(f: E => F[B]): F[B] =
          ApplicativeThrow[F].handleErrorWith(fb):
            case Submarine(e, Marker) => f(e.asInstanceOf[E])
            case t => ApplicativeThrow[F].raiseError(t)

        given Handle[F, E] with
          def applicative = Applicative[F]
          def raise[E2 <: E, B](e: E2): F[B] =
            ApplicativeThrow[F].raiseError(Submarine(e, Marker))
          def handleWith[B](fb: F[B])(f: E => F[B]): F[B] = inner(fb)(f)

        inner(body)(h)
}
