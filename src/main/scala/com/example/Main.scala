package com.example

import com.example.logger._

import java.util.Currency

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
    val eitherPerson: Either[Person, Person] = Left(person1)
    logger.info("eitherPerson" -> eitherPerson)

    // And so do lists
    logger.info("people" -> Seq(person1, person2))

    // And maps
    logger.info("people" -> Map("person1" -> person1, "person2" -> person2))

    // support for exceptions
    logger.error(new IllegalStateException())
    if (logger.info.enabled) {
      // this will take any number of fields but is less efficient
      // as it is not call-by-name
      logger.info.v("p1" -> person1, "p2" -> person2, "p3" -> person1)
    }

    // Render prices in short format in line oriented log format
    val price1 = Price(amount = 8.95, currency = Currency.getInstance("USD"))
    val price2 = Price(amount = 18.95, currency = Currency.getInstance("USD"))

    val prices = Seq(price1, price2)
    logger.info("prices" -> prices)

    // Complex objects are no problem
    val book1 = Book(
      Category("reference"),
      Author("Nigel Rees"),
      Title("Sayings of the Century"),
      price1
    )
    logger.info(book1)
  }
}
