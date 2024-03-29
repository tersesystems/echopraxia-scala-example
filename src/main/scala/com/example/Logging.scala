package com.example

import com.example.logger.LoggingBase
import com.example.logger.LoggingBase.{abbreviateAfter, withAttributes, withStringFormat}
import com.tersesystems.echopraxia.api.{Attributes, Field, Value}

import java.util.{Currency, UUID}
import scala.concurrent.Future
import scala.reflect.{ClassTag, classTag}
import scala.util.{Failure, Success}

// Each package can add its own mappings
trait Logging extends LoggingBase {

  //  format: off
  // Render futures if we have them
  implicit def futureToLog[T: ToValue: ClassTag]: ToLog[Future[T]] = new ToLog[Future[T]] {
    override def toName: ToName[Future[T]] = _ => s"future[${classTag[T].runtimeClass.getName}]"

    override def toValue: ToValue[Future[T]] = f =>
      f.value match {
        case Some(value) =>
          value match {
            case Failure(exception) => ToObjectValue("completed" -> true, "failure" -> exception)
            case Success(value) => ToObjectValue("completed" -> true, "success" -> ToValue(value))
          }
        case None =>
          Value.`object`("completed" -> false)
      }
  }
  //  format: on

  implicit val personToLog: ToLog[Person] = ToLog.create("person", p => ToObjectValue("firstName" -> p.firstName, "lastName" -> p.lastName))

  implicit val titleToLog: ToLog[Title] = ToLog.create("title", t => ToValue(t.raw))

  implicit val authorToLog: ToLog[Author] = ToLog.create("author", a => ToValue(a.raw))

  implicit val categoryToLog: ToLog[Category] = ToLog.create("category", c => ToValue(c.raw))

  implicit val currencyToLog: ToLog[Currency] = ToLog.create("currency", currency => ToValue(currency.getCurrencyCode))

  implicit val priceToLog: ToLog[Price] = ToLog.create("price", price => ToObjectValue(price.currency, "amount" -> price.amount))

  implicit val bookToLog: ToLog[Book] = ToLog.create("book", book => ToObjectValue(book.title, book.category, book.author, book.price))

  // use the class name as the name here
  implicit val uuidToLog: ToLog[UUID] = ToLog.createFromClass(uuid => ToValue(uuid.toString))

  trait ToStringFormat[T] extends ToValueAttribute[T] {
    override def toAttributes(value: Value[_]): Attributes = withAttributes(withStringFormat(value))
  }

  // Says we want a toString of $8.95 in a message template for a price
  implicit val priceToStringValue: ToStringFormat[Price] = (price: Price) => Value.string(price.toString)

  trait AbbreviateAfter[T] extends ToValueAttribute[T] {
    override def toAttributes(value: Value[_]): Attributes = withAttributes(abbreviateAfter(5))
  }

  implicit val titleAbbrev: AbbreviateAfter[Title] = new AbbreviateAfter[Title]() {
    override def toValue(v: Title): Value[_] = Value.string(v.raw)
  }

  // everyone wants different things out of maps, so implementing that
  // is up to the individual application
  implicit def mapToValue[TV: ToValue](implicit va: ToValueAttribute[TV]): ToValue[Map[String, TV]] = { v =>
    val value: Seq[Value.ObjectValue] = v.map { case (k, v) =>
      ToObjectValue("key" -> k, "value" -> v)
    }.toSeq
    ToArrayValue(value)
  }
}
