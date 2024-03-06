package com.example

import com.example.logger.LoggingBase
import com.tersesystems.echopraxia.api.{Field, Value}
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec

import scala.jdk.CollectionConverters._

class ObjectSpec extends AnyWordSpec with Matchers with LoggingBase {

  "object" should {

    "work with a single field" in {
      val field: Field = "test" -> ("foo" -> "bar": Field)
      field.name must be("test")
      val objectValue: Value.ObjectValue = field.value().asObject()
      val fields: Seq[Field] = objectValue.raw.asScala.toSeq
      fields.head.name must be("foo")
    }

    "work with multiple fields" in {
      val field: Field = "test" -> ToObjectValue("foo" -> "bar", "baz" -> "quux")
      field.name must be("test")
      val objectValue: Value.ObjectValue = field.value().asObject()
      val fields: Seq[Field] = objectValue.raw.asScala.toSeq
      fields(1).name must be("baz")
    }

  }

}
