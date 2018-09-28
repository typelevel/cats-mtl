---
layout: docs
title:  "FunctorRaise"
section: "mtlclasses"
source: "core/src/main/scala/cats/mtl/FunctorRaise.scala"
scaladoc: "#cats.mtl.FunctorRaise"
---

## FunctorRaise

`FunctorRaise[F, E]` expresses the ability to raise errors of type `E` in a functorial `F[_]` context.
This means that a value of type `F[A]` may contain no `A` values but instead an `E` error value,
and further `map` calls will not have any values to execute the passed function on.
