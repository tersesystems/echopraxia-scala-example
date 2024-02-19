package com.example.models

import com.example.logger.LoggingBase

import java.util.Currency


// Each package can add its own mappings
trait Logging extends LoggingBase {

  implicit val personToLog: ToLog[Person] = ToLog(
    "person",
    p => ToObjectValue("firstName" -> p.firstName, "lastName" -> p.lastName)
  )

  implicit val titleToLog: ToLog[Title] = ToLog("title", t => ToValue(t.raw))
  implicit val authorToLog: ToLog[Author] = ToLog("author", a => ToValue(a.raw))
  implicit val categoryToLog: ToLog[Category] = ToLog("category", c => ToValue(c.raw))
  implicit val priceToLog: ToLog[Price] = ToLog(
    "price",
    price =>
      ToObjectValue(
        price.currency,
        "amount" -> price.amount
      )
  )

  // Render price as $x.xx when using a line oriented format
  implicit val priceAttributes: ValueAttributes[Price] = (price: Price) => {
    import com.tersesystems.echopraxia.api.Attributes
    Attributes.create(LoggingBase.withStringFormat(price.toString))
  }

  implicit val currencyToLog: ToLog[Currency] =
    ToLog("currency", currency => ToValue(currency.getCurrencyCode))

  implicit val bookToLog: ToLog[Book] = ToLog(
    "book",
    book => {
      ToObjectValue(
        book.title,
        book.category,
        book.author,
      )
    }
  )
}
