package com.example

import com.example.logger.LoggingBase
import com.tersesystems.echopraxia.api.Value

import java.util.{Currency, UUID}

// Each package can add its own mappings
trait Logging extends LoggingBase {

  implicit val currencyToLog: ToLog[Currency] = ToLog.create("currency", currency => ToValue(currency.getCurrencyCode))

  implicit val priceToLog: ToLog[Price] = ToLog.create("price", price => {
    ToObjectValue(price.currency, "amount" -> price.amount)
  })

  // Says we want a toString of $8.95 in a message template for a price
  implicit val priceToStringFormat: ToStringFormat[Price] = (price: Price) => Value.string(price.toString)

  implicit val titleAbbrev: AbbreviateAfter[Title] = new AbbreviateAfter[Title]() {
    override def toValue(v: Title): Value[_] = Value.string(v.raw)
  }
}
