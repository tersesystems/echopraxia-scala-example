package com.example

import com.example.logger.LoggingBase
import com.example.logger.LoggingBase.{withAttributes, withStringFormat}
import com.tersesystems.echopraxia.api.{Attributes, Field, Value}
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec

import java.time.{Instant, LocalDateTime, ZoneOffset}
import java.time.format.{DateTimeFormatter, FormatStyle}

class OptionSpec extends AnyWordSpec with Matchers with LoggingBase {
  implicit val instantToValue: ToValue[Instant] = instant => ToValue(instant.toString)

  // Show a human readable toString
  trait ToStringFormat[T] extends ToValueAttribute[T] {
    override def toAttributes(value: Value[_]): Attributes = withAttributes(withStringFormat(value))
  }

  "option" should {

    "work with primitives" in {
      val field: Field = "test" -> Option(1)
      field.toString must be("test=1")
    }

    "work with Some" in {
      val field: Field = "test" -> Some(1)
      field.toString must be("test=1")
    }

    "work with None" in {
      val field: Field = "test" -> None
      field.toString must be("test=null")
    }

    "work with objects" in {
      val field: Field = "test" -> Option(Instant.ofEpochMilli(0))
      field.toString must be("test=1970-01-01T00:00:00Z")
    }

    "work with custom attributes" in {
      implicit val readableInstant: ToStringFormat[Instant] = (v: Instant) => {
        val datetime = LocalDateTime.ofInstant(v, ZoneOffset.UTC)
        val formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)
        ToValue(formatter.format(datetime))
      }
      val field: Field = "test" -> Option(Instant.ofEpochMilli(0))
      field.toString must be("test=1/1/70, 12:00 AM")
    }
  }
}
