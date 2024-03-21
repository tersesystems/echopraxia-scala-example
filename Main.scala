//> using platform jvm
//> using mainClass Main
//
//> using dep "com.tersesystems.echopraxia.plusscala::api:1.3.1-SNAPSHOT"
//> using dep "com.tersesystems.echopraxia.plusscala::field-logger:1.3.1-SNAPSHOT"
//> using dep "com.tersesystems.echopraxia.plusscala::generic:1.3.1-SNAPSHOT"
//> using dep "com.tersesystems.echopraxia:logstash:3.1.2"
//> using dep "ch.qos.logback:logback-classic:1.5.3"
//> using dep "net.logstash.logback:logstash-logback-encoder:7.4"

import com.tersesystems.echopraxia.api.Field
import com.tersesystems.echopraxia.api.Value

import com.tersesystems.echopraxia.plusscala.fieldlogger._
import com.tersesystems.echopraxia.plusscala.api._
import com.tersesystems.echopraxia.plusscala.generic._

import scala.concurrent.Future

import java.util.{Currency, UUID}
import java.text.NumberFormat
import java.util.Currency

case class Book(category: Category, author: Author, title: Title, price: Price)

case class Price(amount: BigDecimal, currency: Currency) {
  override def toString: String = {
    val numberFormat = NumberFormat.getCurrencyInstance
    numberFormat.setCurrency(currency)
    numberFormat.format(amount)
  }
}

case class Category(raw: String) extends AnyVal
case class Author(raw: String) extends AnyVal
case class Title(raw: String) extends AnyVal

case class Person(firstName: String, lastName: String)

object Main {
  def main(args: Array[String]): Unit = {
    val printer = new Printer()
    printer.print()
  }
}

class Printer extends Logging {
  private val logger: Logger = LoggerFactory.getLogger(getClass)

  def print(): Unit = {
    val person1 = Person("Person1", "Last Name")
    val person2 = Person("Person2", "Last Name")

    // This uses the "personToField" mapping
    logger.info("template shows {}", person1)
    logger.info(person1)

    // Can define custom mapping with tuples
    logger.info("person1" -> person1)
    logger.info("person1" -> person1, "person2" -> person2)

    // Options work out of the box
    val optPerson: Option[Person] = Option(person1)
    logger.info("optPerson" -> optPerson)
    logger.info("optPerson" -> None)

    // As does either
    logger.info("eitherPerson" -> Left(person1))

    // And so do lists
    logger.info("people" -> Seq(person1, person2))

    // If you are rendering tuples or maps, you'll want to specify the field names and values for tuple
    logger.info("tuple" -> (1, person1))

    // And because a Map is an iterable of tuples, this renders as an array of key/value objects
    logger.info("people" -> Map("person1" -> person1, "person2" -> person2))

    // support for exceptions
    val ex = new IllegalStateException("I am an illegal state")
    logger.error(ex)
    if (logger.info.enabled) {
      // this will take any number of fields but is less efficient
      // as it is not call-by-name
      logger.info.v("p1" -> person1, "p2" -> person2, "p3" -> person1)
    }

    // Complex objects are no problem
    val book1 = Book(
      Category("reference"),
      Author("Nigel Rees"),
      Title("Sayings of the Century"),
      Price(amount = 8.95, currency = Currency.getInstance("USD"))
    )
    logger.info(book1)

    // If you want to render fields as an object, you can use ToObjectValue
    logger.info("object" -> ToObjectValue(book1, person1)) // object={book={}, person={}}

    // For heterogeneous fields you'll need to use `Seq[Field]` explicitly, or use info.v as seen below
    logger.info("object" -> Seq[Field](book1, person1)) // object=[book={}, person={}]

    // For heterogeneous values you'll want to specify Seq[Value[_]] to give implicit conversion some clues
    logger.info("oneTrueString" -> Seq(ToValue(1), ToValue(true), ToValue("string")))

    // You can also use "withFields" to render JSON on every message (this will not show in line format)
    logger.withFields(Seq(book1, person1)).info("testing")

    implicit val priceyPriceToFormat: ToValueAttributes[Price] = priceToStringFormat

    // You can also use variadic method but best to wrap it in conditional
    if (logger.info.enabled) {
      // not call by name so it gets evaluated eagerly :-(
      logger.info.v(
          Category("reference"),
          Author("Nigel Rees"),
          Title("Sayings of the Century"),
          person1 // add more than 4
      )
      val price = Price(amount = 8.95, currency = Currency.getInstance("USD"))
      logger.info(price)
      val priceField: Field = "price" -> price
      logger.info(priceField)
    }

    // Can also log using class name
    logger.info(UUID.randomUUID)

    // Logging futures is also possible, and can include names
    logger.info("future[Boolean]" -> Future.successful(true))
    logger.info("future[String]" ->  Future.successful("String"))
  }
}

// Each package can add its own mappings
trait Logging extends LoggingBase with FutureValueTypes with SemiAutoDerivation {
  // Render iterables as arrays (user may want to render as object, so leave this out of LoggingBase)
  implicit def iterableToArrayValue[V: ToValue]: ToArrayValue[Iterable[V]] = ToArrayValue.iterableToArrayValue[V]

  // Render tuples and maps
  implicit def tupleToValue[TVK: ToValue: ToValueAttributes, TVV: ToValue: ToValueAttributes]: ToValue[Tuple2[TVK, TVV]] = {
    case (k, v) => ToObjectValue("key" -> k, "value" -> v)
  }

  implicit val personToLog: ToLog[Person] = ToLog.create("person", p => ToObjectValue("firstName" -> p.firstName, "lastName" -> p.lastName))

  implicit val titleToLog: ToLog[Title] = ToLog.create("title", t => ToValue(t.raw))

  implicit val authorToLog: ToLog[Author] = ToLog.create("author", a => ToValue(a.raw))

  implicit val categoryToLog: ToLog[Category] = ToLog.create("category", c => ToValue(c.raw))

  implicit val currencyToLog: ToLog[Currency] = ToLog.create("currency", currency => ToValue(currency.getCurrencyCode))

  // Says we want a toString of $8.95 in a message template for a price
  implicit val priceToStringFormat: ToStringFormat[Price] = (price: Price) => Value.string(price.toString)

  implicit val priceToLog: ToLog[Price] = ToLog.create("price", price => ToObjectValue(price.currency, "amount" -> price.amount))

  // Case classes can also be auto-generated using macros
  // XXX We want autoderivation using ToLog or we want to use a strategy for it :-/
  // we want a field name configuration like JSON configuration so we can camelcase or snakecase field names?
  // or would it be better to just have a filter for it
  implicit val bookToName: ToName[Book] = ToName.create("book")
  implicit val bookToValue: ToValue[Book] = gen[Book]

  implicit val uuidToLog: ToLog[UUID] = ToLog.create("uuid", uuid => ToValue(uuid.toString))

  implicit val titleAbbrev: AbbreviateAfter[Title] = new AbbreviateAfter[Title]() {
    val after = 5
    override def toValue(v: Title): Value[_] = Value.string(v.raw)
  }
}
