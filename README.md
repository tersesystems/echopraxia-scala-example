# Example Echopraxia + Scala Project

This is a demonstration project that shows the [Scala API for Echopraxia](https://github.com/tersesystems/echopraxia-plusscala) in use and how you can set up logging your way.

The code base is set up so that the domain models used all have a mapping that sets up the names and values used in logging 

```scala
trait Logging extends LoggingBase with FutureValueTypes with HeterogeneousFieldSupport {
  implicit def futureToName[TV: ToValue: ClassTag]: ToName[Future[TV]] = _ => s"future[${classTag[TV].runtimeClass.getName}]"

  // everyone wants different things out of maps, so implementing that
  // is up to the individual application
  implicit def mapToValue[TV: ToValue]: ToValue[Map[String, TV]] = { v =>
    val value: Seq[Value.ObjectValue] = v.map { case (k, v) =>
      ToObjectValue("key" -> k, "value" -> v)
    }.toSeq
    ToArrayValue(value)
  }

  implicit val personToField: ToField[Person] = ToField(_ => "person", p => ToObjectValue("firstName" -> p.firstName, "lastName" -> p.lastName))

  implicit val titleToField: ToField[Title] = ToField(_ => "title", t => ToValue(t.raw).asString().abbreviateAfter(5))

  implicit val authorToField: ToField[Author] = ToField(_ => "author", a => ToValue(a.raw))

  implicit val categoryToField: ToField[Category] = ToField(_ => "category", c => ToValue(c.raw))

  implicit val currencyToField: ToField[Currency] = ToField(_ => "currency", currency => ToValue(currency.getCurrencyCode))

  // Says we want a toString of $8.95 in a message template for a price
  implicit val priceToField: ToField[Price] = ToField(_ => "price", price => ToObjectValue(price.currency, "amount" -> price.amount).withToStringValue(price.toString))

  implicit val bookToField: ToField[Book] = ToField(_ => "book", book => ToObjectValue(book.title, book.category, book.author, book.price))

  implicit val uuidToField: ToField[UUID] = ToField(_ => "uuid", uuid => ToValue(uuid.toString))
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

// You can also use "withFields" to render JSON on every message (this will not show in line format)
logger.withFields(Seq[Field](book1, person1)).info("testing")
```

The logger itself is built on top of the core logger, so it's very simple to extend and customize:

And the technical details are in `LoggerBase`, essentially some type classes and implicit conversions for the logger.