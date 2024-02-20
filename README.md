# Example Echopraxia + Scala Project

This is a demonstration project that shows the [Scala API for Echopraxia](https://github.com/tersesystems/echopraxia-plusscala) in use and how you can set up logging your way.

The code base is set up so that the domain models used all have a mapping that sets up the names and values used in logging 

```scala
trait Logging extends LoggingBase {

  implicit val personToLog: ToLog[Person] = 
    ToLog("person", p => ToObjectValue("firstName" -> p.firstName, "lastName" -> p.lastName))
  
  implicit val bookToLog: ToLog[Book] = 
    ToLog("book", book => ToObjectValue(book.title, book.category, book.author, book.price))

  implicit val uuidToLog: ToLog[UUID] = 
    ToLog.fromClassName(uuid => ToValue(uuid.toString))

  // Render price as $x.xx when using a line oriented format instead of rendering the child fields
  implicit val priceAttributes: ValueAttributes[Price] = (price: Price) => Attributes.create(withStringFormat(price.toString))
}
```

and structured logging can either be done with arguments alone, or using tuples:

```scala
val person1 = Person("Person1", "Last Name")
val person2 = Person("Person2", "Last Name")

// This uses the "personToField" mapping
logger.info("template shows {}", person1)
logger.info(person1)

// Can define custom mapping with tuples
logger.info("person1" -> person1, "person2" -> person2)

// Options work out of the box
val optPerson: Option[Person] = Option(person1)
logger.info("optPerson" -> optPerson)

// As does either
val eitherPerson: Either[Person, Person] = Left(person1)
logger.info("eitherPerson" -> eitherPerson)

// And so do lists
logger.info("people" -> Seq(person1, person2))

// And maps
logger.info("people" -> Map("person1" -> person1, "person2" -> person2))
```

The logger itself is built on top of the core logger.  It's very simple to extend and customize:

```scala
class Logger(core: CoreLogger) {

  // Add extra methods as needed for moar functionality
  def withCondition(condition: Condition): Logger = new Logger(core.withCondition(condition.asJava))

  abstract class LoggerMethod(level: Level) {
    def enabled: Boolean = core.isEnabled(level.asJava)

    def apply(message: String): Unit = core.log(level.asJava, message)
    def apply(message: String, f1: => Field): Unit = handle(level, message, f1)
    def apply(message: String, f1: => Field, f2: => Field): Unit = handle(level, message, f1 ++ f2)

    def apply(message: String, f1: => Field, f2: => Field, f3: => Field): Unit = {
      handle(level, message, f1 ++ f2 ++ f3)
    }

    def apply(message: String, f1: => Field, f2: => Field, f3: => Field, f4: => Field): Unit = {
      handle(level, message, f1 ++ f2 ++ f3 ++ f4)
    }

    private def handle(level: Level, message: String, f: => FieldBuilderResult): Unit = {
      import scala.compat.java8.FunctionConverters._

      val f1: PresentationFieldBuilder => FieldBuilderResult = _ => f
      core.log(level.asJava, message, f1.asJava, PresentationFieldBuilder)
    }

    def apply(f1: => Field): Unit = handle(level, "{}", f1)

    def apply(f1: => Field, f2: => Field): Unit = handle(level, "{} {}", f1 ++ f2)

    def apply(f1: => Field, f2: => Field, f3: => Field): Unit =
      handle(level, "{} {} {}", f1 ++ f2 ++ f3)

    def apply(f1: => Field, f2: => Field, f3: => Field, f4: => Field): Unit = {
      handle(level, "{} {} {} {}", f1 ++ f2 ++ f3 ++ f4)
    }

    // This will eagerly evaluate all fields, regardless of logger level :-(
    def v(fields: Field*): Unit =
      handle(level, fields.map(_ => "{}").mkString(" "), list(fields.toArray))
  }

  object info extends LoggerMethod(INFO)

  object debug extends LoggerMethod(DEBUG)

  object trace extends LoggerMethod(TRACE)

  object warn extends LoggerMethod(WARN)

  object error extends LoggerMethod(ERROR)
}
```

And the technical details are in `LoggerBase`, essentially some type classes and implicit conversions for the logger.