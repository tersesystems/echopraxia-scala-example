package com.example

import com.example.logger.LoggingBase
import com.example.logger.LoggingBase.{withSeqStringFormat, withStringFormat}
import com.tersesystems.echopraxia.api.Attributes

import java.util.Currency

// Each package can add its own mappings
trait Logging extends LoggingBase {

  implicit val personToLog: ToLog[Person] = ToLog("person", p => ToObjectValue("firstName" -> p.firstName, "lastName" -> p.lastName))

  implicit val titleToLog: ToLog[Title] = ToLog("title", t => ToValue(t.raw))

  implicit val authorToLog: ToLog[Author] = ToLog("author", a => ToValue(a.raw))

  implicit val categoryToLog: ToLog[Category] = ToLog("category", c => ToValue(c.raw))

  implicit val currencyToLog: ToLog[Currency] = ToLog("currency", currency => ToValue(currency.getCurrencyCode))

  implicit val priceToLog: ToLog[Price] = ToLog("price", price => ToObjectValue(price.currency, "amount" -> price.amount))

  implicit val bookToLog: ToLog[Book] = ToLog("book", book => ToObjectValue(book.title, book.category, book.author, book.price))

  // Render price as $x.xx when using a line oriented format instead of rendering the child fields
  implicit val priceAttributes: ValueAttributes[Price] = (price: Price) => Attributes.create(withStringFormat(price.toString))

  // collections of values are a special case :-(
  implicit val priceSeqAttributes: ValueAttributes[Seq[Price]] = (prices: Seq[Price]) =>
    Attributes.create(withSeqStringFormat(prices.map(_.toString)))

  implicit val priceSetAttributes: ValueAttributes[Set[Price]] = (prices: Set[Price]) => priceSeqAttributes.attributes(prices.toSeq)
}
