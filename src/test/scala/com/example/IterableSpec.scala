package com.example

import com.example.logger.LoggingBase
import com.example.logger.LoggingBase.{withAttributes, withStringFormat}
import com.tersesystems.echopraxia.api.{Attributes, Field, Value}
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec

import java.time.format.{DateTimeFormatter, FormatStyle}
import java.time.{Instant, LocalDateTime, ZoneOffset}
import scala.jdk.CollectionConverters.SeqHasAsJava


class IterableSpec extends AnyWordSpec with Matchers with LoggingBase {

  "iterable" should {
    implicit val instantToValue: ToValue[Instant] = instant => ToValue(instant.toString)

    // Show a human readable toString
    trait ToStringFormat[T] extends ToValueAttribute[T] {
      override def toAttributes(value: Value[_]): Attributes = withAttributes(withStringFormat(value))
    }

    "work for primitives" in {
      val seq: Seq[Int] = Seq(1,2,3)
      val field: Field = "test" -> seq
      field.name must be("test")
      field.value must be(Value.array(seq.map(ToValue(_)).asJava))
      field.toString must be("test=[1, 2, 3]")
    }

    "work for objects" in {
      val instant1 = Instant.ofEpochMilli(0)
      val instant2 = Instant.ofEpochMilli(1000000)
      val field: Field = "test" -> Seq(instant1, instant2)
      field.toString must be("test=[1970-01-01T00:00:00Z, 1970-01-01T00:16:40Z]")
    }

    "work for objects with custom attributes" in {
      implicit val readableInstant: ToStringFormat[Instant] = (v: Instant) => {
        val datetime = LocalDateTime.ofInstant(v, ZoneOffset.UTC)
        val formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)
        ToValue(formatter.format(datetime))
      }

      val instant1 = Instant.ofEpochMilli(0)
      val instant2 = Instant.ofEpochMilli(1000000)
      val field: Field = "test" -> Seq(instant1, instant2)
      field.toString must be("test=[1/1/70, 12:00 AM, 1/1/70, 12:16 AM]")
    }

    "work with set" in {
      val instant1 = Instant.ofEpochMilli(0)
      val instant2 = Instant.ofEpochMilli(1000000)
      val field: Field = "test" -> Set(instant1, instant2)
      field.toString must be("test=[1970-01-01T00:00:00Z, 1970-01-01T00:16:40Z]")
    }

    "work with fields using ToArrayValue" in {
      val instant1 = Instant.ofEpochMilli(0)
      val instant2 = Instant.ofEpochMilli(1000000)

      val fields = Seq[Field]("instant1" -> instant1, "instant2" -> instant2)
      val field: Field = "test" -> ToArrayValue(fields)
      field.toString must be("test=[{instant1=1970-01-01T00:00:00Z}, {instant2=1970-01-01T00:16:40Z}]")
    }

    "work with fields with just plain fields" in {
      val instant1 = Instant.ofEpochMilli(0)
      val instant2 = Instant.ofEpochMilli(1000000)

      val fields = Seq[Field]("instant1" -> instant1, "instant2" -> instant2)
      val field: Field = "test" -> fields
      field.toString must be("test=[{instant1=1970-01-01T00:00:00Z}, {instant2=1970-01-01T00:16:40Z}]")
    }
  }

}
