package com.example

import com.example.logger._
import com.tersesystems.echopraxia.api.{Field, Value}

import java.util.Currency

object Main {
  def main(args: Array[String]): Unit = {
    val printer = new PricePrinter()
    printer.print()
  }
}

class PricePrinter extends Logging {
  private val logger: Logger = LoggerFactory.getLogger(getClass)

  def print(): Unit = {
    // Render prices in short format in line oriented log format
    val price1 = Price(amount = 8.95, currency = Currency.getInstance("USD"))
    val price2 = Price(amount = 18.95, currency = Currency.getInstance("USD"))

    val prices = Seq(price1, price2)
    logger.info("optionPrice" -> Option(price1))

    logger.info("prices" -> prices)
    logger.info("priceSet" -> Set(price1, price2))
    logger.info("priceMap" -> Map("key1" -> price1))
    logger.info("priceOption" -> Option(price1))
    logger.info("priceNone" -> Option[Price](null))

    def eitherPrice(p: Price): Either[Throwable, Price] = {
      Right(p)
    }
    logger.info("priceEither" -> eitherPrice(price1))

    //
    //    // Complex objects are no problem
    //    val book1 = Book(
    //      Category("reference"),
    //      Author("Nigel Rees"),
    //      Title("Sayings of the Century"),
    //      price1
    //    )
    //    logger.info(book1)
  }

  def either(price: Price): Either[Price, Book] = Left(price)
}

