package cats.mtl.bench

import cats.data.StateT
import cats.effect.IO
import org.openjdk.jmh.annotations._
import java.util.concurrent.TimeUnit

import cats.mtl.special.StateIO

@BenchmarkMode(Array(Mode.Throughput))
@OutputTimeUnit(TimeUnit.SECONDS)
class StateBench {

  def leftFlatMapSpecial(bound: Int): Int = {
    def loop(i: Int): StateIO[Int, Int] =
      if (i > bound) StateIO.pure(i)
      else StateIO.pure[Int, Int](i + 1).flatMap(loop)

    StateIO.pure[Int, Int](0).flatMap(loop).unsafeRunSyncS(0)
  }

  def leftFlatMapStateT(bound: Int): Int = {
    def loop(i: Int): StateT[IO, Int, Int] =
      if (i > bound) StateT.pure[IO, Int, Int](i)
      else StateT.pure[IO, Int, Int](i + 1).flatMap(loop)

    StateT.pure[IO, Int, Int](0).flatMap(loop).runS(0).unsafeRunSync()
  }

  @Benchmark
  def leftAssociatedBindSpecial(): Int = leftFlatMapSpecial(100000)

  @Benchmark
  def leftAssociatedBindStateT(): Int = leftFlatMapStateT(100000)

}
