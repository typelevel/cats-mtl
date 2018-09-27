---
layout: page
title:  "Getting Started"
section: "gettingstarted"
position: 2
---


## What is MTL?


#### MTL classes

cats-mtl provides the following "MTL classes":
 - `ApplicativeAsk`
 - `ApplicativeLocal`
 - `FunctorRaise`
 - `ApplicativeHandle`
 - `FunctorTell`
 - `FunctorListen`
 - `MonadState`
 - `MonadChroncicle`

All of these are typeclasses built on top of other "base" typeclasses to provide extra laws.
Because they are commonly combined, the base typeclasses are ambiguous in implicit scope using
the typical cats subclass encoding via subtyping. Thus in cats-mtl, the ambiguity is avoided by using
an implicit scope with a different priority to house each conversion `Subclass => Base`.

Compared to cats and Haskell's mtl library, cats-mtl's typeclasses have the smallest superclass dependencies
practically possible and in some cases smaller sets of operations so that they can be lifted over more transformers.
