package com.example

import com.example.logger.LoggingBase
import com.example.logger.LoggingBase._
import com.tersesystems.echopraxia.api.{Attributes, Value}

import java.util
import java.util.{Currency, UUID}
import scala.jdk.CollectionConverters.{IteratorHasAsJava, SeqHasAsJava}
import scala.jdk.OptionConverters.RichOption

// Each package can add its own mappings
trait Logging extends LoggingBase {

  implicit val personToLog: ToLog[Person] = ToLog.create("person", p => ToObjectValue("firstName" -> p.firstName, "lastName" -> p.lastName))

  implicit val titleToLog: ToLog[Title] = ToLog.create("title", t => ToValue(t.raw))

  implicit val authorToLog: ToLog[Author] = ToLog.create("author", a => ToValue(a.raw))

  implicit val categoryToLog: ToLog[Category] = ToLog.create("category", c => ToValue(c.raw))

  implicit val currencyToLog: ToLog[Currency] = ToLog.create("currency", currency => ToValue(currency.getCurrencyCode))

  implicit val priceToLog: ToLog[Price] = ToLog.create("price", price => ToObjectValue(price.currency, "amount" -> price.amount))

  implicit val bookToLog: ToLog[Book] = ToLog.create("book", book => ToObjectValue(book.title, book.category, book.author, book.price))

  implicit val uuidToLog: ToLog[UUID] = ToLog.createFromClass(uuid => ToValue(uuid.toString))

  // Render price as $x.xx when using a line oriented format instead of rendering the child fields
  implicit val priceAttributes: ValueAttributes[Price] = (price: Price) => withStringFormat {
    Value.string(price.toString)
  }

  // option is a special case :-(
  implicit val priceOptionAttributes: ValueAttributes[Option[Price]] = (price: Option[Price]) => withStringFormat {
    val optValue: Option[Value.StringValue] = price.map(p => Value.string(p.toString))
    Value.optional(optValue.toJava)
  }

  // collections of values are also a special case :-( :-(
  implicit def priceIterableAttributes[T <: Iterable[Price]]: ValueAttributes[T] = (prices: T) => withStringFormat {
      val seq: Seq[Value[_]] = prices.map(p => Value.string(p.toString).asInstanceOf[Value[_]]).toSeq
      Value.array(seq.asJava)
    }

}
