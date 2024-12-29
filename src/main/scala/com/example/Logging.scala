package com.example

import echopraxia.plusscala.api._
import echopraxia.plusscala.generic._

import scala.concurrent.Future
import java.util.{Currency, UUID}
import scala.reflect.{ClassTag, classTag}

// Each package can add its own mappings
trait Logging extends EchopraxiaBase with HeterogeneousFieldSupport with SemiAutoDerivation {
  implicit def futureToName[TV: ToValue: ClassTag]: ToName[Future[TV]] = _ => s"future[${classTag[TV].runtimeClass.getName}]"

  // Echopraxia takes a bit more work the more heterogeneous the input gets.
  // For example, to pass through random tuples, you need to map it to an object
  implicit def tupleToValue[TVK: ToValue, TVV: ToValue]: ToValue[(TVK, TVV)] = {
    case (k, v) => ToObjectValue("key" -> k, "value" -> v)
  }

  implicit val personToField: ToField[Person] = ToField(_ => "person", p => ToObjectValue("firstName" -> p.firstName, "lastName" -> p.lastName))

  implicit val titleToField: ToField[Title] = ToField(_ => "title", t => ToValue(t.raw).asString().abbreviateAfter(5))

  implicit val authorToField: ToField[Author] = ToField(_ => "author", a => ToValue(a.raw))

  implicit val categoryToField: ToField[Category] = ToField(_ => "category", c => ToValue(c.raw))

  implicit val currencyToField: ToField[Currency] = ToField(_ => "currency", currency => ToValue(currency.getCurrencyCode))

  // Says we want a toString of $8.95 in a message template for a price
  implicit val priceToField: ToField[Price] = ToField(_ => "price", price => ToObjectValue(price.currency, "amount" -> price.amount).withToStringValue(price.toString))

  // For case classes, we can use macros to generate mappings for us
  //implicit val bookToField: ToField[Book] = ToField(_ => "book", book => ToObjectValue(book.title, book.category, book.author, book.price))
  implicit val bookToName: ToName[Book] = _ => "book"
  implicit val bookToValue: ToValue[Book] = gen[Book]

  implicit val uuidToField: ToField[UUID] = ToField(_ => "uuid", uuid => ToValue(uuid.toString))
}
