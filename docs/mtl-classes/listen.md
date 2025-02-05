## Listen

`Listen` extends `Tell` and expands upon its ability to write to a log, by also providing a way to access the log when given a value `F[A]`. 

This means, given an `F[A]` we can use `listen` to see the accumulated value inside, by turning it into an `F[(A, L)]`.
That comes in handy whenever you want to do something with your accumulated log.
For example, we might want to send our logs to some external server.
Let's have a look at an example of how one could do this:


```scala mdoc
import cats._
import cats.data._
import cats.syntax.all._
import cats.mtl._
import cats.mtl.implicits._

def sendToServer[F[_]: Monad](logs: Chain[String]): F[Unit] =
  // impure implementation for demonstrative purposes, please don't do this at home
  Monad[F].pure(println(show"Sending to server: $logs"))

def sendLogsToServer[F[_]: Monad, A](logProgram: F[A])(implicit F: Listen[F, Chain[String]]): F[A] =
  logProgram.listen.flatMap {
    case (a, logs) => sendToServer[F](logs).as(a)
  }
```

Now we have a function `sendLogsToServer` that takes any program with logging and will send those logs to the server.
To see if it works, let's write some logging program and run it all with `Writer`.

```scala mdoc

def logging[F[_]: Monad](implicit F: Tell[F, Chain[String]]): F[Unit] =
  // Example of some logging activity in your application
  for {
    _ <- F.tell(Chain.one("First log"))
    _ <- F.tell(Chain.one("Second log"))
  } yield ()

val result = sendLogsToServer(logging[Writer[Chain[String], *]]).value
```

And there, we can see it prints out the log.
In summary `Listen` can be used to access or "listen to" the logged values,
 which is pretty useful whenever you want to actually do something with the logs.
