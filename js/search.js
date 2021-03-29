// When the user clicks on the search box, we want to toggle the search dropdown
function displayToggleSearch(e) {
  e.preventDefault();
  e.stopPropagation();

  closeDropdownSearch(e);
  
  if (idx === null) {
    console.log("Building search index...");
    prepareIdxAndDocMap();
    console.log("Search index built.");
  }
  const dropdown = document.querySelector("#search-dropdown-content");
  if (dropdown) {
    if (!dropdown.classList.contains("show")) {
      dropdown.classList.add("show");
    }
    document.addEventListener("click", closeDropdownSearch);
    document.addEventListener("keydown", searchOnKeyDown);
    document.addEventListener("keyup", searchOnKeyUp);
  }
}

//We want to prepare the index only after clicking the search bar
var idx = null
const docMap = new Map()

function prepareIdxAndDocMap() {
  const docs = [  
    {
      "title": "Ask",
      "url": "/cats-mtl/mtl-classes/ask.html",
      "content": "Ask Ask[F, E] allows us to read a value of type E from a shared environment into a context F[_]. In practical terms, this means, that if we have an instance of Ask[F, E], we can now request a value of F[E] by using the ask method. Simplified, it is defined like this: trait Ask[F[_], E] { def ask: F[E] } This typeclass comes in handy whenever we want to be able to request a value from the environment, e.g. read from some external configuration. Instances A trivial instance for Ask can be just a plain function: def functionAsk[E]: Ask[E =&gt; ?, E] = new Ask[E =&gt; ?, E] { def ask: E =&gt; E = identity } Other instances include Reader and ReaderT/Kleisli. In fact the implementation for ask is just ReaderT.ask, so it’s a perfect fit. Cats-mtl is able to lift Ask instances through a monad transformer stack, so for example, StateT[Reader[E, ?], S, A] and EitherT[ReaderT[List, E, ?], Error, A] will both have Ask instances whenever you import from cats.mtl.instances._. In general every monad transformer stack where Reader appears once, will have an instance of Ask."
    } ,    
    {
      "title": "Chronicle",
      "url": "/cats-mtl/mtl-classes/chronicle.html",
      "content": "Chronicle Chronicle[F, E] allows us to both accumulate values and also short-circuit computations with a final output. In that sense it can be seen as a hybrid of Tell and Handle/MonadError. Chronicle is defined by 3 distinct operators, to understand it better, let’s look at a simplified definition: trait Chronicle[F[_], E] { def dictate(c: E): F[Unit] def confess[A](c: E): F[A] def materialize[A](fa: F[A]): F[E Ior A] } If you’re already familiar with Tell, dictate should look familiar. In fact, it has the same signature as Tell#tell and achieves roughly the same thing, appending a value of E to some internal log. Next, we have confess, which might seems familar if you know Raise/ApplicativeError’s raise/raiseError` method. It also does the same thing here, which is raising an error that will short-circuit the computation. This means, that internally, instances of Chronicle need to differentiate between values of type E. dictate adds a value of E to a log, but allows the computation to continue. Values coming from confess on the other hand, stop all computations afterwards and short-circuit, since calls to flatMap will no longer have any effect. The only way to recover from the short-circuiting behaviour of confess is to use Chronicle’s third method materialize. materialize takes an F[A] and returns an F[E Ior A]. Ior represents an inclusive-or relationship, meaning an E Ior A can either be an E, an A or a tuple of both (E, A). You can read more about Ior in the cats documentation. These three cases perfectly represent in which state the F[A] was in. If the F[A] passed to materialize was short-circuited by confess, then the result will be only an E. If F[A] at some point involved a call to dictate, meaning it had accumulated a log of E, then the result will be a pair of the log E and the resulting value A. If neither confess or dictate were used to construct the F[A], then materialize will only return the resulting value A. To gain a better understanding, let’s look at an example. Some applications call for a notion of non-fatal errors, along with the more standard fatal errors that halt computations. Chronicle is perfect for these kinds of use cases. Let’s say we want to create a sign up where users can register with username and password. We can prohibit bad user data by using a computation halting confess and nudge them in the right direction by giving warnings when e.g. their password is fairly short: import cats.Monad import cats.data._ import cats.implicits._ import cats.mtl.Chronicle type Failures = NonEmptyChain[String] case class Username(value: String) case class Password(value: String) case class User(name: Username, pw: Password) def validateUsername[F[_]: Monad](u: String)(implicit F: Chronicle[F, Failures]): F[Username] = { if (u.isEmpty) F.confess(NonEmptyChain.one(\"Can't be empty\")) else if (u.contains(\".\")) F.dictate(NonEmptyChain.one(\"Dot in name is deprecated\")).map(_ =&gt; Username(u)) else Username(u).pure[F] } def validatePassword[F[_]: Monad](p: String)(implicit F: Chronicle[F, Failures]): F[Password] = { if (p.length &lt; 8) F.confess(NonEmptyChain.one(\"Password too short\")) else if (p.length &lt; 10) F.dictate(NonEmptyChain.one(\"Password should be longer\")).map(_ =&gt; Password(p)) else Password(p).pure[F] } def validateUser[F[_]: Monad](name: String, password: String)(implicit F: Chronicle[F, Failures]): F[User] = (validateUsername[F](name), validatePassword[F](password)).mapN(User) Now we can fully validate users and accumulate a log of warnings or fail entirely when an invalid name or password was encountered. Pretty neat, next let’s actually run this program to see if what we did was correct. We can do that with Ior: val luka = validateUser[Ior[Failures, ?]](\"Luka\", \"secret\") // luka: Ior[cats.data.NonEmptyChainImpl.Type[String], User] = Left( // a = Singleton(a = \"Password too short\") // ) val john = validateUser[Ior[Failures, ?]](\"john.doe\", \"secret123\") // john: Ior[cats.data.NonEmptyChainImpl.Type[String], User] = Both( // a = Append( // leftNE = Singleton(a = \"Dot in name is deprecated\"), // rightNE = Singleton(a = \"Password should be longer\") // ), // b = User( // name = Username(value = \"john.doe\"), // pw = Password(value = \"secret123\") // ) // ) val jane = validateUser[Ior[Failures, ?]](\"jane\", \"reallysecurepassword\") // jane: Ior[cats.data.NonEmptyChainImpl.Type[String], User] = Right( // b = User( // name = Username(value = \"jane\"), // pw = Password(value = \"reallysecurepassword\") // ) // ) Apart from Ior instances for Chronicle also include its IorT and any monad transformer stack where Ior or IorT appear."
    } ,    
    {
      "title": "Contributing",
      "url": "/cats-mtl/contributing.html",
      "content": "Contributing Adding a new transformer typeclass First thing is the set of operations, or the signature. For example Tell[F, E] has the signature def tell(e: E): F[Unit]. Once you’ve got this down, you need to come up with a set of laws and a base typeclass. These have to be worked out at the same time, because choosing base classes with more operations gives you richer potential for laws but restricts implementations severely. Come up with a rough sketch of the laws in Scala as methods but assuming no type parameters need to be provided and all of the operations needed are in scope; use &lt;-&gt; for the equality assertion. To be continued. Adding a new transformer typeclass instance The characteristic which transformer typeclasses have in common is that instances of them are commonly lifted through transformer stacks. I.e., if you have an Ask[E =&gt; ?], you can have an Ask[EitherT[E =&gt; ?, String, ?]]; a so-called inductive instance of Ask, because the capability is not provided by EitherT itself but the “inner monad”. Because of this, there’s an argument to be made that there’s only one transformer typeclass instance you should need to provide: that of the “innermost” monad, the one that is actually implementing the typeclass operations."
    } ,    
    {
      "title": "Design",
      "url": "/cats-mtl/design.html",
      "content": "cats-mtl Design Overall, cats-mtl has similar design to cats: instances package, syntax package, implicits package. There’s also a hierarchy package, because cats-mtl uses a different typeclass encoding than cats to avoid implicit ambiguity. The problem looks like this: import cats._ type Err type Log def f[F[_]: MonadError[?, Err]: MonadWriter[?, Log]]: Unit = { // implicitly[Monad[F]] // ambiguity between derived monads from MonadError and MonadWriter // 1.pure[F].flatMap(_ =&gt; 1.raise) // same ambiguity affects MonadSyntax () } The cause is that the implicit conversions MonadError[F, Err] =&gt; Monad[F] and MonadWriter[F, Log] =&gt; Monad[F] are the same priority. If those conversions were different priorities, the issue would be fixed, and that’s exactly what cats-mtl’s encoding does. Similarly to the Scato project, instead of inheritance the transformer classes in cats-mtl use aggregation. Mtl classes thus contain instances of their superclasses. Motivation The motivation for cats-mtl’s existence can be summed up in a few points: using subtyping to express typeclass subclassing results in implicit ambiguities, and doing it another way would result in a massive inconsistency inside cats if only done for MTL classes. for a detailed explanation, see Adelbert Chang’s article here. most MTL classes do not actually require Monad as a constraint for their laws. cats-mtl weakens this constraint to Functor or Applicative whenever possible, with the result that there’s now a notion of a Functor transformer stack and Applicative transformer stack in addition to that of a Monad transformer stack. the most used operations on MonadWriter and MonadReader are tell and ask, and the other operations severely restrict the space of implementations despite being used much less. To fix this Listen and Local are subclasses of Tell and Ask, which have only the essentials. The first point there means that it’s impossible for cats-mtl type classes to expose their base class instances implicitly; for example F[_]: Stateful[?[_], S] isn’t enough for a Monad[F] to be visible in implicit scope, despite Stateful containing a Monad instance as a member. The root cause here is that prioritizing implicit conversions with subtyping explicitly can’t work with cats and cats-mtl separate, as the Monad[F] instance for the type from cats will always conflict with a derived instance. Thus F[_]: Stateful[?[_], S], translated, becomes F[_]: Monad: Stateful[?[_], S]. For some historical info on the origins of cats-mtl, see: https://github.com/typelevel/cats/issues/1210 https://github.com/typelevel/cats/pull/1379 https://github.com/typelevel/cats/pull/1751 Laws Type class laws come in a few varieties in cats-mtl: internal, external, and free. Internal laws dictate how multiple operations inter-relate. One side of the equation can always be reduced to a single function application of an operation. These express “default” implementations that should be indistinguishable in result from the actual implementation. External laws are laws that still need to be tested but don’t fall into the internal laws. Free laws are (in theory) unnecessary to test, because they are implied by other laws and the types of the operations in question. There will usually be rudimentary proofs or some justification attached to make sure these aren’t just made up."
    } ,    
    {
      "title": "Getting Started",
      "url": "/cats-mtl/getting-started.html",
      "content": "What is MTL? MTL is an acronym and stands for Monad Transformer Library. Its main purpose is to make it easier to work with nested monad transformers. It achieves this by encoding the effects of most common monad transformers as type classes. The problem Have you ever worked with two or more monad transformers nested inside each other? If you haven’t, working with what some call monad transformer stacks can be incredibly painful. This is because, the more monad transformers you add to your stack the more type parameters the type system has to deal with and the worse type inference gets. For example, here’s a small example of a method that reads the current state in StateT and raises an error using EitherT: import cats.data._ def checkState: EitherT[StateT[List, Int, ?], Exception, String] = for { currentState &lt;- EitherT.liftF(StateT.get[List, Int]) result &lt;- if (currentState &gt; 10) EitherT.leftT[StateT[List, Int, ?], String](new Exception) else EitherT.rightT[StateT[List, Int, ?], Exception](\"All good\") } yield result There’s a bunch of type annotations and extra machinery that really has nothing to do with the actual program and this problem only gets worse as you add more expressions to your for-comprehension or add an additional monad transformer to the stack. Thankfully, Cats-mtl is here to help. How Cats-mtl helps Monad Transformers encode some notion of effect EitherT encodes the effect of short-circuiting errors. ReaderT encodes the effect of reading a value from the environment. StateT encodes the effect of pure local mutable state. All of these monad transformers encode their effects as data structures, but there’s another way to achieve the same result: Type classes! For example take ReaderT.ask function, what would it look like if we used a type class here instead? Well, Cats-mtl has an answer and it’s called Ask. You can think of it as ReaderT encoded as a type class: trait Ask[F[_], E] { val applicative: Applicative[F] def ask: F[E] } At its core Ask just encodes the fact that we can ask for a value from the environment, exactly like ReaderT does. Exactly like ReaderT, it also includes another type parameter E, that represents that environment. If you’re wondering why Ask has an Applicative field instead of just extending from Applicative, that is to avoid implicit ambiguities that arise from having multiple subclasses of a given type (here Applicative) in scope implicitly. So in this case we favor composition over inheritance as otherwise, we could not e.g. use Monad together with Ask. Ask is an example for what is at the core of Cats-mtl. Cats-mtl provides type classes for most common effects which let you choose what kind of effects you need without commiting to a specific monad transformer stack. Ideally, you’d write all your code using only an abstract type constructor F[_] with different type class constraints and then at the end run that code with a specific data type that is able to fulfill those constraints. So without further ado, let’s look at what the program from earlier looks when using Cats-mtl: First, the original program again: import cats.data._ def checkState: EitherT[StateT[List, Int, ?], Exception, String] = for { currentState &lt;- EitherT.liftF(StateT.get[List, Int]) result &lt;- if (currentState &gt; 10) EitherT.leftT[StateT[List, Int, ?], String](new Exception(\"Too large\")) else EitherT.rightT[StateT[List, Int, ?], Exception](\"All good\") } yield result And now the mtl version: import cats.MonadError import cats.syntax.all._ import cats.mtl.Stateful def checkState[F[_]](implicit S: Stateful[F, Int], E: MonadError[F, Exception]): F[String] = for { currentState &lt;- S.get result &lt;- if (currentState &gt; 10) E.raiseError(new Exception(\"Too large\")) else E.pure(\"All good\") } yield result We’ve reduced the boilerplate immensly! Now our small program actually looks just like control flow and we didn’t need to annotate any types. This is great so far, but checkState now returns an abstract F[String]. We need some F that has an instance of both Stateful and MonadError. We know that EitherT can have a MonadError instance and StateT can have a Stateful instance, but how do we get both? Fortunately, cats-mtl allows us to lift instances of mtl classes through our monad transformer stack. That means, that e.g. EitherT[StateT[List, Int, ?], Exception, A] has both the MonadError and Stateful instances we need, neat! So let’s try turning our abstract F[String] into an actual value: val materializedProgram = checkState[EitherT[StateT[List, Int, ?], Exception, ?]] This process of turning a program defined by an abstract type constructor with additional type class constraints into an actual concrete data type is sometimes called interpreting or materializing a program. Usually we don’t have to do this until the very end where we want to run the full application, so the only place we see actual monad transformers is at the very edge. In summary Cats-mtl provides two things: MTL type classes representing effects and a way to lift instances of these classes through transformer stacks. We suggest you start learning the MTL classes first and learn how lifting works later, as you don’t need to understand lifting at all to enjoy all the benefits of Cats-mtl. Available MTL classes cats-mtl provides the following “MTL classes”: Ask Local Raise Handle Tell Listen Censor Stateful Chronicle If you’re wondering why some of these are based on Monad and others on Functor or Applicative, it is because these are the smallest superclass dependencies practically possible with which you can define their laws. This is in contrast to Haskell’s mtl library and earlier versions of Cats as well as Scalaz, this gives us less restrictions over which type classes can be lifted over which transformers. Because these type classes are commonly combined, the base typeclasses are ambiguous in implicit scope using the typical cats subclass encoding via inheritance. Thus in cats-mtl, the ambiguity is avoided by using the composition approach that we saw with Ask earlier. In a future version of Scala we might be able to avoid this and have the same type class encoding everywhere."
    } ,    
    {
      "title": "Handle",
      "url": "/cats-mtl/mtl-classes/handle.html",
      "content": "Handle Handle[F, E] extends Raise with the ability to also handle raised errors. It adds the handleWith function, which can be used to recover from errors and therefore allow continuing computations. The handleWith function has the following signature: def handleWith[A](fa: F[A])(f: E =&gt; F[A]): F[A] This function is fully equivalent to handleErrorWith from cats.ApplicativeError and in general Handle is fully equivalent to cats.ApplicativeError, but is not a subtype of Applicative and therefore doesn’t cause any ambiguitites. Let’s look at an example of how to use this function: import cats._ import cats.implicits._ import cats.mtl._ import cats.mtl.implicits._ def parseNumber[F[_]: Applicative](in: String)(implicit F: Raise[F, String]): F[Int] = { // this function might raise an error if (in.matches(\"-?[0-9]+\")) in.toInt.pure[F] else F.raise(show\"'$in' could not be parsed as a number\") } def notRecovered[F[_]: Applicative](implicit F: Raise[F, String]): F[Boolean] = { parseNumber[F](\"foo\") .map(n =&gt; if (n &gt; 5) true else false) } def recovered[F[_]: Applicative](implicit F: Handle[F, String]): F[Boolean] = { parseNumber[F](\"foo\") .handle[String](_ =&gt; 0) // Recover from error with fallback value .map(n =&gt; if (n &gt; 5) true else false) } val err = notRecovered[Either[String, ?]] // err: Either[String, Boolean] = Left( // value = \"'foo' could not be parsed as a number\" // ) val result = recovered[Either[String, ?]] // result: Either[String, Boolean] = Right(value = false)"
    } ,    
    {
      "title": "Home",
      "url": "/cats-mtl/",
      "content": "Cats MTL Provides transformer typeclasses for cats’ Monads, Applicatives and Functors. You can have multiple cats-mtl transformer typeclasses in scope at once without implicit ambiguity, unlike in pre-1.0.0 cats or Scalaz 7. Usage libraryDependencies += \"org.typelevel\" %% \"cats-mtl\" % \"1.1.1\" If your project uses ScalaJS, replace the double-% with a triple. Note that cats-mtl has an upstream dependency on cats-core version 2.x. Cross-builds are available for Scala 2.12, 2.13, 3.0.0-M2, 3.0.0-M3, and ScalaJS major version 1.x. If you’re not sure where to start or what Cats MTL even is, please refer to the getting started guide. Supported Classes EitherT Kleisli IorT OptionT ReaderWriterStateT StateT WriterT Laws The cats-mtl-laws artifact provides Discipline-style laws for all of the type classes defined in cats-mtl. It is relatively easy to use these laws to test your own implementations of these typeclasses. Take a look here for more. libraryDependencies += \"org.typelevel\" %% \"cats-mtl-laws\" % \"1.1.1\" % Test These laws are compatible with both Specs2 and ScalaTest. Documentation Links: Website: typelevel.org/cats-mtl/ ScalaDoc: typelevel.org/cats-mtl/api/ Related Cats links (the core): Website: typelevel.org/cats/ ScalaDoc: typelevel.org/cats/api/ Community People are expected to follow the Scala Code of Conduct when discussing cats-mtl on the Github page, Gitter channel, or other venues. We hope that our community will be respectful, helpful, and kind. If you find yourself embroiled in a situation that becomes heated, or that fails to live up to our expectations, you should disengage and contact one of the project maintainers in private. We hope to avoid letting minor aggressions and misunderstandings escalate into larger problems. License All code is available to you under the MIT license, available at http://opensource.org/licenses/mit-license.php and also in the COPYING file."
    } ,      
    {
      "title": "Listen",
      "url": "/cats-mtl/mtl-classes/listen.html",
      "content": "Listen Listen extends Tell and expands upon its ability to write to a log, by also providing a way to access the log when given a value F[A]. This means, given an F[A] we can use listen to see the accumulated value inside, by turning it into an F[(A, L)]. That comes in handy whenever you want to do something with your accumulated log. For example, we might want to send our logs to some external server. Let’s have a look at an example of how one could do this: import cats._ import cats.data._ import cats.implicits._ import cats.mtl._ import cats.mtl.implicits._ def sendToServer[F[_]: Monad](logs: Chain[String]): F[Unit] = // impure implementation for demonstrative purposes, please don't do this at home Monad[F].pure(println(show\"Sending to server: $logs\")) def sendLogsToServer[F[_]: Monad, A](logProgram: F[A])(implicit F: Listen[F, Chain[String]]): F[A] = logProgram.listen.flatMap { case (a, logs) =&gt; sendToServer[F](logs).as(a) } Now we have a function sendLogsToServer that takes any program with logging and will send those logs to the server. To see if it works, let’s write some logging program and run it all with Writer. def logging[F[_]: Monad](implicit F: Tell[F, Chain[String]]): F[Unit] = // Example of some logging activity in your application for { _ &lt;- F.tell(Chain.one(\"First log\")) _ &lt;- F.tell(Chain.one(\"Second log\")) } yield () val result = sendLogsToServer(logging[Writer[Chain[String], ?]]).value // Sending to server: Chain(First log, Second log) And there, we can see it prints out the log. In summary Listen can be used to access or “listen to” the logged values, which is pretty useful whenever you want to actually do something with the logs."
    } ,    
    {
      "title": "Local",
      "url": "/cats-mtl/mtl-classes/local.html",
      "content": "Local Local[F, E] extends Ask and allows to us express local modifications of the environment. In practice, this means, that whenever we use local on a value of ask, we can locally modify the environment with a function E =&gt; E. In this case, locally modifying means that the only place we can actually observe the modification is in the F[A] value passed to the local function. The local function has the following signature: trait Local[F[_], E] extends Ask[F, E] { def local(f: E =&gt; E)(fa: F[A]): F[A] } Here’s a quick example of how it works: import cats._ import cats.data._ import cats.implicits._ import cats.mtl._ def calculateContentLength[F[_]: Applicative](implicit F: Ask[F, String]): F[Int] = F.ask.map(_.length) def calculateModifiedContentLength[F[_]: Applicative](implicit F: Local[F, String]): F[Int] = F.local(calculateContentLength[F])(\"Prefix \" + _) val result = calculateModifiedContentLength[Reader[String, ?]].run(\"Hello\") // result: Int = 12 We can see that the content length returned is 12, because the string \"Prefix \" is prepended to the environment value. To see that local only applies to the passed parameter we can run both calculateContentLength and calculateModifiedContentLength in one for-expression: def both[F[_]: Monad](implicit F: Local[F, String]): F[(Int, Int)] = for { length &lt;- calculateContentLength[F] modifiedLength &lt;- calculateModifiedContentLength[F] } yield (length, modifiedLength) val res = both[Reader[String, ?]].run(\"Hello\") // res: (Int, Int) = (5, 12)"
    } ,    
    {
      "title": "Migration Guide",
      "url": "/cats-mtl/migration.html",
      "content": "Migration guide Here’s a map from pre-1.x cats typeclasses to cats-mtl typeclasses: MonadReader --&gt; Local MonadWriter --&gt; Listen Stateful --&gt; Stateful cats typeclass parameters and context bounds have to be rewritten, to include base classes. For example: [F[_]: MonadReader[?[_], E]] will have to be adjusted to [F[_]: Monad: Local[?[_], E]], [F[_]: MonadWriter[?[_], L]] will have to be adjusted to [F[_]: Monad: Listen[?[_], L]], [F[_]: Stateful[?[_], S]] will have to be adjusted to [F[_]: Monad: Stateful[?[_], S]] The root cause for this is addressed in the motivation section. Ask / Local MonadReader from cats has been split into Ask and Local; the Monad constraint has been weakened to Applicative, and the local method was split out into a subclass to widen the implementation space for Ask. Tell / Listen Similarly to MonadReader, MonadWriter was split into Tell and Listen and the constraint weakened to Functor, and Listen being Tell with the listen method added."
    } ,    
    {
      "title": "MTL Type Classes",
      "url": "/cats-mtl/mtl-classes.html",
      "content": "MTL classes summary Tell[F, L] is the ability to “log” values L inside a context F[_], as an effect. The primary monad transformer instance for Tell is WriterT. Listen[F, L] extends Tell and gives you the ability to expose the state that is contained in all F[A] values, and that can be modified using tell. In that sense it is equivalent to a function F[A] =&gt; F[(A, L)]. The primary monad transformer instance for Listen is WriterT. Ask[F, E] lets you access an E value in the F[_] context. Intuitively, this means that an E value is required as an input to get “out” of the F[_] context. The primary monad transformer instance for Ask is ReaderT. Local[F, E] extends Ask and lets you substitute all of the ask occurrences in an F[A] value with ask.map(f), for some f: E =&gt; E which produces a modified E input. The primary monad transformer instance for Local is ReaderT. Stateful[F, S] is the capability to access and modify a state value from inside the F[_] context, using set(s: S): F[Unit] and get: F[S]. The primary monad transformer instance for Stateful is StateT. Chronicle[F, E] is the ability to both accumulate outputs and aborting computation with a final output. In that sense it can be seen as a hybrid of Handle and Tell. The primary monad transformer instance for Chronicle is IorT. Raise[F, E] expresses the ability to raise errors of type E in a functorial F[_] context. This means that a value of type F[A] may contain no A values but instead an E error value, and further map calls will not have any values to execute the passed function on. The primary monad transformer instance for Raise is EitherT. Handle[F, E] extends Raise with the ability to also handle raised errors. It is fully equivalent to cats.ApplicativeError, but is not a subtype of Applicative and therefore doesn’t cause any ambiguitites. The primary monad transformer instance for Handle is EitherT."
    } ,    
    {
      "title": "Raise",
      "url": "/cats-mtl/mtl-classes/raise.html",
      "content": "Raise Raise[F, E] expresses the ability to raise errors of type E in a functorial F[_] context. This means that a value of type F[A] may contain no A values but instead an E error value, and further map calls will not have any effect due to there being no value to execute the passed function on. In that sense it is a less powerful version of ApplicativeError found in cats-core. In general, however, raiseError from ApplicativeError and raise from Raise should be equivalent. The raise function has the following signature: trait Raise[F[_], E] { def raise[A](e: E): F[A] } Here’s a quick example of how you might use it: import cats._ import cats.implicits._ import cats.mtl._ def parseNumber[F[_]: Applicative](in: String)(implicit F: Raise[F, String]): F[Int] = { if (in.matches(\"-?[0-9]+\")) in.toInt.pure[F] else F.raise(show\"'$in' could not be parsed as a number\") } val valid = parseNumber[Either[String, ?]](\"123\") // valid: Either[String, Int] = Right(value = 123) val invalid = parseNumber[Either[String, ?]](\"123abc\") // invalid: Either[String, Int] = Left( // value = \"'123abc' could not be parsed as a number\" // )"
    } ,      
    {
      "title": "Stateful",
      "url": "/cats-mtl/mtl-classes/stateful.html",
      "content": "Stateful Stateful[F, S] describes the capability to read and write state values of type S inside the F[_] context. This materializes in two functions get: F[S] and set: S =&gt; F[Unit] that are needed for Stateful instances: trait Stateful[F[_], S] { def get: F[S] def set(s: S): F[Unit] } Simply spoken Stateful gives us the ability to thread state through computations in F[_]. It provides access to controlled mutable state, which can be very useful, but must also be used with care. There are dozens of use cases where it might make sense to use Stateful, one of them is to use it to maintain a cache when repeatedly accessing external services. If we’ve already accessed a service with a given identifier, we can then access the cache instead, of course we also want the ability to invalidate the cache to receive fresh values as well. Let’s say we have the following function to access a service: import cats._ import cats.data._ import cats.implicits._ import cats.mtl.Stateful case class ServiceResult(id: Int, companies: List[String]) def serviceCall[F[_]: Monad](id: String): F[ServiceResult] = { // a fake call to some external service, impure, so don't do this at home! println(show\"Called service with $id\") ServiceResult(0, List(\"Raven Enterprises\")).pure[F] } Now, we want a new function that looks inside of a cache for the desired value and if it’s not there, access the service and put the result inside the cache. To do so, we’ll make use of Stateful with a simple Map[String, ServiceResult] as our cache: type Cache = Map[String, ServiceResult] def cachedServiceCall[F[_]: Monad](id: String)(implicit F: Stateful[F, Cache]): F[ServiceResult] = for { cache &lt;- F.get result &lt;- cache.get(id) match { case Some(result) =&gt; result.pure[F] case None =&gt; serviceCall[F](id) } } yield result So far, so good, we’re successfully reading from the cache if it has the value we need, but it also doesn’t actually write anything into the cache when we call our service. Let’s try again to write to the cache after making the service call. def serviceCallAndWriteToCache[F[_]: Monad](id: String)(implicit F: Stateful[F, Cache]): F[ServiceResult] = for { result &lt;- serviceCall[F](id) cache &lt;- F.get _ &lt;- F.set(cache.updated(id, result)) } yield result Lastly, we want to be able to invalidate our cache. This is fairly simple, as all we need to do is to use set with an empty cache: def invalidate[F[_]](implicit F: Stateful[F, Cache]): F[Unit] = F.set(Map.empty) Now that we have our building blocks, we can now build a program that makes a few requests then invalidates the cache and makes another request: def program[F[_]: Monad](implicit F: Stateful[F, Cache]): F[ServiceResult] = for { result1 &lt;- cachedServiceCall[F](\"ab94d2\") result2 &lt;- cachedServiceCall[F](\"ab94d2\") // This should use the cached value _ &lt;- invalidate[F] freshResult &lt;- cachedServiceCall[F](\"ab94d2\") // This should access the service again } yield freshResult And we’re done, now, to be able to run this, we need to materialize our program into an actual value. As the name suggests, State and StateT both allow us to use Stateful: val initialCache: Cache = Map.empty // initialCache: Cache = Map() val (result, cache) = program[State[Cache, ?]].run(initialCache).value // Called service with ab94d2 // Called service with ab94d2 // Called service with ab94d2 // result: Map[String, ServiceResult] = Map() // cache: ServiceResult = ServiceResult( // id = 0, // companies = List(\"Raven Enterprises\") // ) As per usual, Cats-mtl provides Stateful instances for all monad transformer stacks where StateT appears."
    } ,    
    {
      "title": "Tell",
      "url": "/cats-mtl/mtl-classes/tell.html",
      "content": "Tell Data types that have instances of Tell[F, L] are able to accumulate values of type L in the F[_] context. This is done by adding values of L to the accumulated log by using the tell function: trait Tell[F[_], L] { def tell(l: L): F[Unit] } This log is write-only and the only way to modify it is to add more L values. Usually most instances require a Monoid[L] in order to properly accumulate values of L. Let’s have a look at an example how you could Tell to log service calls. First we’ll add the actual service call with its parameters and result types: import cats._ import cats.data._ import cats.implicits._ import cats.mtl.Tell case class ServiceParams(option1: String, option2: Int) case class ServiceResult(userId: Int, companies: List[String]) def serviceCall[F[_]: Monad](params: ServiceParams): F[ServiceResult] = // a fake call to some external service, replace with real implementation ServiceResult(0, List(\"Raven Enterprises\")).pure[F] Now let’s write an extra function that log what kind of parameters were passed as well as what the service returned. For the log, we’ll be using a Chain[String], as it supports very efficient concatenation: def serviceCallWithLog[F[_]: Monad](params: ServiceParams)(implicit F: Tell[F, Chain[String]]): F[ServiceResult] = for { _ &lt;- F.tell(Chain.one(show\"Call to service with ${params.option1} and ${params.option2}\")) result &lt;- serviceCall[F](params) _ &lt;- F.tell(Chain.one(show\"Service returned: userId: ${result.userId}; companies: ${result.companies}\")) } yield result Now let’s materialize this to see if it worked. Instances for Tell include Writer and WriterT, but also Tuple2: val (log, result): (Chain[String], ServiceResult) = serviceCallWithLog[Writer[Chain[String], ?]](ServiceParams(\"business\", 42)).run // log: Chain[String] = Append( // leftNE = Singleton(a = \"Call to service with business and 42\"), // rightNE = Singleton( // a = \"Service returned: userId: 0; companies: List(Raven Enterprises)\" // ) // ) // result: ServiceResult = ServiceResult( // userId = 0, // companies = List(\"Raven Enterprises\") // ) And voila, it works as expected. We were using just a standard Writer for this example, but remember that any monad transformer stack with WriterT in it somewhere will have an instance of Tell."
    }    
  ];

  idx = lunr(function () {
    this.ref("title");
    this.field("content");

    docs.forEach(function (doc) {
      this.add(doc);
    }, this);
  });

  docs.forEach(function (doc) {
    docMap.set(doc.title, doc.url);
  });
}

// The onkeypress handler for search functionality
function searchOnKeyDown(e) {
  const keyCode = e.keyCode;
  const parent = e.target.parentElement;
  const isSearchBar = e.target.id === "search-bar";
  const isSearchResult = parent ? parent.id.startsWith("result-") : false;
  const isSearchBarOrResult = isSearchBar || isSearchResult;

  if (keyCode === 40 && isSearchBarOrResult) {
    // On 'down', try to navigate down the search results
    e.preventDefault();
    e.stopPropagation();
    selectDown(e);
  } else if (keyCode === 38 && isSearchBarOrResult) {
    // On 'up', try to navigate up the search results
    e.preventDefault();
    e.stopPropagation();
    selectUp(e);
  } else if (keyCode === 27 && isSearchBarOrResult) {
    // On 'ESC', close the search dropdown
    e.preventDefault();
    e.stopPropagation();
    closeDropdownSearch(e);
  }
}

// Search is only done on key-up so that the search terms are properly propagated
function searchOnKeyUp(e) {
  // Filter out up, down, esc keys
  const keyCode = e.keyCode;
  const cannotBe = [40, 38, 27];
  const isSearchBar = e.target.id === "search-bar";
  const keyIsNotWrong = !cannotBe.includes(keyCode);
  if (isSearchBar && keyIsNotWrong) {
    // Try to run a search
    runSearch(e);
  }
}

// Move the cursor up the search list
function selectUp(e) {
  if (e.target.parentElement.id.startsWith("result-")) {
    const index = parseInt(e.target.parentElement.id.substring(7));
    if (!isNaN(index) && (index > 0)) {
      const nextIndexStr = "result-" + (index - 1);
      const querySel = "li[id$='" + nextIndexStr + "'";
      const nextResult = document.querySelector(querySel);
      if (nextResult) {
        nextResult.firstChild.focus();
      }
    }
  }
}

// Move the cursor down the search list
function selectDown(e) {
  if (e.target.id === "search-bar") {
    const firstResult = document.querySelector("li[id$='result-0']");
    if (firstResult) {
      firstResult.firstChild.focus();
    }
  } else if (e.target.parentElement.id.startsWith("result-")) {
    const index = parseInt(e.target.parentElement.id.substring(7));
    if (!isNaN(index)) {
      const nextIndexStr = "result-" + (index + 1);
      const querySel = "li[id$='" + nextIndexStr + "'";
      const nextResult = document.querySelector(querySel);
      if (nextResult) {
        nextResult.firstChild.focus();
      }
    }
  }
}

// Search for whatever the user has typed so far
function runSearch(e) {
  if (e.target.value === "") {
    // On empty string, remove all search results
    // Otherwise this may show all results as everything is a "match"
    applySearchResults([]);
  } else {
    const tokens = e.target.value.split(" ");
    const moddedTokens = tokens.map(function (token) {
      // "*" + token + "*"
      return token;
    })
    const searchTerm = moddedTokens.join(" ");
    const searchResults = idx.search(searchTerm);
    const mapResults = searchResults.map(function (result) {
      const resultUrl = docMap.get(result.ref);
      return { name: result.ref, url: resultUrl };
    })

    applySearchResults(mapResults);
  }

}

// After a search, modify the search dropdown to contain the search results
function applySearchResults(results) {
  const dropdown = document.querySelector("div[id$='search-dropdown'] > .dropdown-content.show");
  if (dropdown) {
    //Remove each child
    while (dropdown.firstChild) {
      dropdown.removeChild(dropdown.firstChild);
    }

    //Add each result as an element in the list
    results.forEach(function (result, i) {
      const elem = document.createElement("li");
      elem.setAttribute("class", "dropdown-item");
      elem.setAttribute("id", "result-" + i);

      const elemLink = document.createElement("a");
      elemLink.setAttribute("title", result.name);
      elemLink.setAttribute("href", result.url);
      elemLink.setAttribute("class", "dropdown-item-link");

      const elemLinkText = document.createElement("span");
      elemLinkText.setAttribute("class", "dropdown-item-link-text");
      elemLinkText.innerHTML = result.name;

      elemLink.appendChild(elemLinkText);
      elem.appendChild(elemLink);
      dropdown.appendChild(elem);
    });
  }
}

// Close the dropdown if the user clicks (only) outside of it
function closeDropdownSearch(e) {
  // Check if where we're clicking is the search dropdown
  if (e.target.id !== "search-bar") {
    const dropdown = document.querySelector("div[id$='search-dropdown'] > .dropdown-content.show");
    if (dropdown) {
      dropdown.classList.remove("show");
      document.documentElement.removeEventListener("click", closeDropdownSearch);
    }
  }
}
