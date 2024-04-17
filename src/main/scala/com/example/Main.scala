package com.example

import com.tersesystems.echopraxia.api.Field
import com.tersesystems.echopraxia.plusscala._

import java.util.{Currency, UUID}
import scala.concurrent.Future

object Main {
  def main(args: Array[String]): Unit = {
    val printer = new Printer()
    printer.print()
  }
}

class Printer extends Logging {
  private val logger = LoggerFactory.getLogger(getClass)

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

    // and maps (with a little work to set up what you want the field names to be)
    logger.info("people" -> Map("person1" -> person1, "person2" -> person2))

    // and tuples
    logger.info("tuple" -> (1, person1))

    // support for exceptions
    logger.error(new IllegalStateException())

    if (logger.isInfoEnabled) {
      logger.info("p1" -> person1, "p2" -> person2, "p3" -> person1)
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
    logger.withFields(Seq[Field](book1, person1)).info("testing")

    if (logger.isInfoEnabled) {
      logger.info(
          Category("reference"),
          Author("Nigel Rees"),
          Title("Sayings of the Century"),
          Price(amount = 8.95, currency = Currency.getInstance("USD")),
          person1 // add more than 4
      )
    }

    // Can also log using class name
    logger.info(UUID.randomUUID)

    // Logging futures is also possible, and can include names
    logger.info(Future.successful(true))
    logger.info(Future.successful("String"))
  }
}
