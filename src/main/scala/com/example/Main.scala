package com.example

import com.example.logger._
import com.tersesystems.echopraxia.api.Field

import java.util
import java.util.{Currency, UUID}
import scala.concurrent.Future
import scala.jdk.CollectionConverters.SeqHasAsJava

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

    // and maps
    logger.info("people" -> Map("person1" -> person1, "person2" -> person2))

    // Echopraxia takes a bit more work the more heterogeneous the input gets.
    // For example, to pass through random tuples, you need to map it to an object
    implicit def tupleToValue[TVK: ToValue, TVV: ToValue](implicit va: ToValueAttribute[Tuple2[TVK, TVV]]): ToValue[Tuple2[TVK, TVV]] = {
      case (k, v) => ToObjectValue("_1" -> k, "_2" -> v)
    }
    logger.info("tuple" -> (1, person1))

    // support for exceptions
    logger.error(new IllegalStateException())
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

    // Logging more than four parameters does mean passing a Seq[Field] through,
    // you can expand the logger API if this is an issue
    val fields: Seq[Field] = Seq(
      Category("reference"),
      Author("Nigel Rees"),
      Title("Sayings of the Century"),
      Price(amount = 8.95, currency = Currency.getInstance("USD")),
      person1 // add more than 4
    )
    logger.info(fields)

    // Also this deals with Java lists
    val javaList: util.List[Field] = fields.asJava
    logger.info(javaList)

    // You can also use variadic method but best to wrap it in conditional
    if (logger.info.enabled) {
      // not call by name so it gets evaluated eagerly :-(
      logger.info.v(
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
