---
layout: docs
title:  "MonadChronicle"
section: "mtlclasses"
source: "core/src/main/scala/cats/mtl/MonadChronicle.scala"
scaladoc: "#cats.mtl.MonadChronicle"
---

## MonadChronicle

`MonadChronicle[F, E]` is the ability to both accumulate outputs and aborting computation with a final output.
In that sense it can be seen as a hybrid of `ApplicativeHandle` and `FunctorTell`.