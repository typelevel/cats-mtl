package cats.mtl.special

import cats.Monad
import cats.effect.concurrent.Ref
import cats.effect._
import cats.mtl.MonadState


/* Performant counterpart to StateT[IO, S, A] */
object StateIO extends StateIOInstances with StateIOImpl with Newtype2 {

  private[cats] def wrap[S, A](s: IO[A]): Type[S, A] =
    s.asInstanceOf[Type[S, A]]

  private[cats] def unwrap[S, A](e: Type[S, A]): IO[A] =
    e.asInstanceOf[IO[A]]

  @inline
  def liftF[S, A](fa: IO[A]): StateIO[S, A] = wrap(fa)

  def get[S]: StateIO[S, S] = liftF(refInstance[S].get)

  def modify[S](f: S => S): StateIO[S, Unit] = liftF(refInstance[S].update(f))

  def set[S](s: S): StateIO[S, Unit] = liftF(refInstance[S].set(s))

  def pure[S, A](a: A): StateIO[S, A] = liftF(IO.pure(a))

  def create[S, A](f: S => IO[(S, A)]): StateIO[S, A] =
    liftF(refInstance[S].get.flatMap(s => f(s).flatMap {
      case (s, a) => refInstance[S].set(s).map(_ => a)
    }))

  implicit def stateIOOps[S, A](v: StateIO[S, A]): StateIOOps[S, A] =
    new StateIOOps[S, A](v)
}

private[special] class StateIOOps[S, A](val sp: StateIO[S, A]) extends AnyVal {

  def unsafeRunAsync(s: S)(f: Either[Throwable, (S, A)] => Unit): Unit =
    StateIO.refInstance[S].set(s)
      .flatMap(_ => StateIO.unwrap(sp).flatMap(a => StateIO.refInstance[S].get.map(s => (s, a))))
      .unsafeRunAsync(f)

  def unsafeRunSync(s: S): (S, A) =
    StateIO.refInstance[S].set(s)
      .flatMap(_ => StateIO.unwrap(sp).flatMap(a => StateIO.refInstance[S].get.map(s => (s, a))))
      .unsafeRunSync()

  def unsafeRunSyncA(s: S): A =
    StateIO.refInstance[S].set(s).flatMap(_ => StateIO.unwrap(sp)).unsafeRunSync()

  def unsafeRunSyncS(s: S): S =
    StateIO.refInstance[S].set(s)
      .flatMap(_ => StateIO.unwrap(sp).flatMap(_ => StateIO.refInstance[S].get))
      .unsafeRunSync()
}


private[special] abstract class StateIOAsync[S] extends Async[StateIO[S, ?]] {

  def flatMap[A, B](fa: StateIO[S, A])(f: A => StateIO[S, B]): StateIO[S, B] =
    StateIO.wrap(StateIO.unwrap(fa).flatMap(a => StateIO.unwrap(f(a))))

  def suspend[A](thunk: => StateIO[S, A]): StateIO[S, A] =
    StateIO.liftF(IO.suspend(StateIO.unwrap(thunk)))

  def bracketCase[A, B](acquire: StateIO[S, A])
                       (use: A => StateIO[S, B])
                       (release: (A, ExitCase[Throwable]) => StateIO[S, Unit]): StateIO[S, B] =
    StateIO.liftF(StateIO.unwrap(acquire)
      .bracketCase(a => StateIO.unwrap(use(a)))((a, ec) => StateIO.unwrap(release(a, ec))))

  def async[A](k: (Either[Throwable, A] => Unit) => Unit): StateIO[S, A] =
    StateIO.liftF(IO.async(k))

  def asyncF[A](k: (Either[Throwable, A] => Unit) => StateIO[S, Unit]): StateIO[S, A] =
    StateIO.liftF(IO.asyncF(cb => StateIO.unwrap(k(cb))))

  override def map[A, B](fa: StateIO[S, A])(f: A => B): StateIO[S, B] =
    StateIO.liftF(StateIO.unwrap(fa).map(f))

  def pure[A](x: A): StateIO[S, A] = StateIO.pure(x)

  def raiseError[A](e: Throwable): StateIO[S, A] = StateIO.liftF(IO.raiseError(e))

  def handleErrorWith[A](fa: StateIO[S, A])(f: Throwable => StateIO[S, A]): StateIO[S, A] =
    StateIO.liftF(StateIO.unwrap(fa).handleErrorWith(e => StateIO.unwrap(f(e))))

  def tailRecM[A, B](a: A)(f: A => StateIO[S, Either[A, B]]): StateIO[S, B] =
    StateIO.liftF(Monad[IO].tailRecM(a)(a => StateIO.unwrap(f(a))))
}

private[special] sealed abstract class StateIOInstances extends StateIOInstances0 {
  implicit def stateIOMonadState[S]: MonadState[StateIO[S, ?], S] = new MonadState[StateIO[S, ?], S] {
    val monad: Monad[StateIO[S, ?]] = stateIOAsync[S]

    def get: StateIO[S, S] = StateIO.get[S]

    def modify(f: S => S): StateIO[S, Unit] = StateIO.modify(f)

    def set(s: S): StateIO[S, Unit] = StateIO.set(s)

    def inspect[A](f: S => A): StateIO[S, A] = monad.map(StateIO.get)(f)
  }

  implicit def stateIOConcurrent[S](implicit ctx: ContextShift[IO]): Concurrent[StateIO[S, ?]] =
    new StateIOAsync[S] with Concurrent[StateIO[S, ?]] {

      type Fiber[A] = cats.effect.Fiber[StateIO[S, ?], A]

      override def cancelable[A](k: (Either[Throwable, A] => Unit) => CancelToken[StateIO[S, ?]]): StateIO[S, A] =
        StateIO.liftF(IO.cancelable[A](cb => StateIO.unwrap(k(cb))))

      def start[A](fa: StateIO[S, A]): StateIO[S, Fiber[A]] =
        StateIO.liftF[S, Fiber[A]](StateIO.unwrap(fa).start.map(fiberT))


      def racePair[A, B](fa: StateIO[S, A], fb: StateIO[S, B]): StateIO[S, Either[(A, Fiber[B]), (Fiber[A], B)]] =
        StateIO.liftF(IO.racePair(StateIO.unwrap(fa), StateIO.unwrap(fb)).map {
          case Left((a, fib)) => Left((a, fiberT(fib)))
          case Right((fib, b)) => Right((fiberT(fib), b))
        })

      protected def fiberT[A](fiber: cats.effect.Fiber[IO, A]): Fiber[A] =
        Fiber(StateIO.liftF(fiber.join), StateIO.liftF(fiber.cancel))
    }
}

private[special] sealed abstract class StateIOInstances0 {
  implicit def stateIOAsync[S]: Async[StateIO[S, ?]] =
    new StateIOAsync[S]{}
}


private[special] sealed trait StateIOImpl {

  /* There be dragons */
  // scalastyle:off null
  private val ref: Ref[IO, Any] = Ref.unsafe[IO, Any](null)
  // scalastyle:on null

  private[special] def refInstance[S]: Ref[IO, S] = ref.asInstanceOf[Ref[IO, S]]
}
