package cats.mtl.special

import cats.Monad
import cats.effect.concurrent.Ref
import cats.effect._
import cats.mtl.MonadState

/* Performant counterpart to StateT[IO, S, A] */
case class StateIO[S, A]private (private val value: IO[A]) {
  def unsafeRunAsync(s: S)(f: Either[Throwable, (S, A)] => Unit): Unit =
    StateIO.refInstance[S].set(s)
      .flatMap(_ => value.flatMap(a => StateIO.refInstance[S].get.map(s => (s, a))))
      .unsafeRunAsync(f)

  def unsafeRunSync(s: S): (S, A) =
    StateIO.refInstance[S].set(s)
      .flatMap(_ => value.flatMap(a => StateIO.refInstance[S].get.map(s => (s, a))))
      .unsafeRunSync()

  def unsafeRunSyncA(s: S): A =
    StateIO.refInstance[S].set(s).flatMap(_ => value).unsafeRunSync()

  def unsafeRunSyncS(s: S): S =
    StateIO.refInstance[S].set(s)
      .flatMap(_ => value.flatMap(_ => StateIO.refInstance[S].get))
      .unsafeRunSync()

  def flatMap[B](f: A => StateIO[S, B]): StateIO[S, B] =
    StateIO(value.flatMap(a => f(a).value))

  def map[B](f: A => B): StateIO[S, B] =
    StateIO(value.map(f))

  def get: StateIO[S, S] =
    flatMap(_ => StateIO.get[S])
}



object StateIO extends StateIOImpl {

  def liftF[S, A](fa: IO[A]): StateIO[S, A] = StateIO(fa)

  def get[S]: StateIO[S, S] = StateIO(refInstance[S].get)

  def modify[S](f: S => S): StateIO[S, Unit] = StateIO(refInstance[S].update(f))

  def set[S](s: S): StateIO[S, Unit] = StateIO(refInstance[S].set(s))

  def pure[S, A](a: A): StateIO[S, A] = StateIO(IO.pure(a))

  def create[S, A](f: S => IO[(S, A)]): StateIO[S, A] =
    StateIO(refInstance[S].get.flatMap(s => f(s).flatMap {
      case (s, a) => refInstance[S].set(s).map(_ => a)
    }))

  implicit def monadStateStateIO[S](implicit ctx: ContextShift[IO]): MonadState[StateIO[S, ?], S] = new MonadState[StateIO[S, ?], S] {
    val monad: Monad[StateIO[S, ?]] = monadErrorStateIO

    def get: StateIO[S, S] = StateIO.get[S]

    def modify(f: S => S): StateIO[S, Unit] = StateIO.modify(f)

    def set(s: S): StateIO[S, Unit] = StateIO.set(s)

    def inspect[A](f: S => A): StateIO[S, A] = StateIO.get.map(f)
  }

  implicit def monadErrorStateIO[S](implicit ctx: ContextShift[IO]): Concurrent[StateIO[S, ?]] =
    new Concurrent[StateIO[S, ?]] {

      type Fiber[A] = cats.effect.Fiber[StateIO[S, ?], A]

      def flatMap[A, B](fa: StateIO[S, A])(f: A => StateIO[S, B]): StateIO[S, B] =
        fa.flatMap(f)

      override def suspend[A](thunk: => StateIO[S, A]): StateIO[S, A] =
        StateIO.liftF(IO.suspend(thunk.value))

      def bracketCase[A, B](acquire: StateIO[S, A])
                           (use: A => StateIO[S, B])
                           (release: (A, ExitCase[Throwable]) => StateIO[S, Unit]): StateIO[S, B] =
        StateIO(acquire.value.bracketCase(a => use(a).value)((a, ec) => release(a, ec).value))

      def async[A](k: (Either[Throwable, A] => Unit) => Unit): StateIO[S, A] =
        StateIO.liftF(IO.async(k))

      def asyncF[A](k: (Either[Throwable, A] => Unit) => StateIO[S, Unit]): StateIO[S, A] =
        StateIO.liftF(IO.asyncF(cb => k(cb).value))

      override def cancelable[A](k: (Either[Throwable, A] => Unit) => CancelToken[StateIO[S, ?]]): StateIO[S, A] =
        StateIO.liftF(IO.cancelable[A](cb => k(cb).value))

      def start[A](fa: StateIO[S, A]): StateIO[S, Fiber[A]] =
        StateIO.liftF[S, Fiber[A]](fa.value.start.map(fiberT))


      def racePair[A, B](fa: StateIO[S, A], fb: StateIO[S, B]): StateIO[S, Either[(A, Fiber[B]), (Fiber[A], B)]] =
        StateIO(IO.racePair(fa.value, fb.value).map {
          case Left((a, fib)) => Left((a, fiberT(fib)))
          case Right((fib, b)) => Right((fiberT(fib), b))
        })


      override def map[A, B](fa: StateIO[S, A])(f: A => B): StateIO[S, B] = fa.map(f)

      def pure[A](x: A): StateIO[S, A] = StateIO.pure(x)

      def raiseError[A](e: Throwable): StateIO[S, A] = StateIO(IO.raiseError(e))

      def handleErrorWith[A](fa: StateIO[S, A])(f: Throwable => StateIO[S, A]): StateIO[S, A] =
        StateIO(fa.value.handleErrorWith(e => f(e).value))

      def tailRecM[A, B](a: A)(f: A => StateIO[S, Either[A, B]]): StateIO[S, B] =
        StateIO(Monad[IO].tailRecM(a)(a => f(a).value))

      protected def fiberT[A](fiber: cats.effect.Fiber[IO, A]): Fiber[A] =
        Fiber(StateIO(fiber.join), StateIO(fiber.cancel))
    }
}



private[special] sealed trait StateIOImpl {

  /* There be dragons */
  private val ref: Ref[IO, Any] = Ref.unsafe[IO, Any](null)

  private[special] def refInstance[S]: Ref[IO, S] = ref.asInstanceOf[Ref[IO, S]]
}
