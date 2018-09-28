---
layout: docs
title:  "ApplicativeHandle"
section: "mtlclasses"
source: "core/src/main/scala/cats/mtl/ApplicativeHandle.scala"
scaladoc: "#cats.mtl.ApplicativeHandle"
---

## ApplicativeHandle

`ApplicativeHandle[F, E]` extends `FunctorRaise` with the ability to also handle raised errors.
It is fully equivalent to `cats.ApplicativeError`, but is not a subtype of `Applicative` and therefore doesn't cause any ambiguitites.
